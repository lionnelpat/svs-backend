package sn.svs.backoffice.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
// Import retiré - on utilisera le nom complet com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sn.svs.backoffice.domain.Expense;
import sn.svs.backoffice.dto.ExpenseDTO;
import sn.svs.backoffice.repository.ExpenseRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'export des dépenses en PDF et Excel
 * SVS - Dakar, Sénégal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseExportService {

    private final ExpenseRepository expenseRepository;

    @Value("${app.export.storage-path:/tmp/exports}")
    private String exportStoragePath;

    // Couleurs du thème SVS
    private static final Color SVS_PRIMARY = new DeviceRgb(30, 64, 175);        // #1e40af
    private static final Color SVS_HIGHLIGHT = new DeviceRgb(6, 182, 212);      // #06b6d4
    private static final Color SVS_SURFACE_100 = new DeviceRgb(241, 245, 249);  // #f1f5f9
    private static final Color SVS_SURFACE_200 = new DeviceRgb(226, 232, 240);  // #e2e8f0
    private static final Color SVS_WHITE = new DeviceRgb(255, 255, 255);        // #ffffff

    /**
     * Exporte les dépenses au format PDF
     */
    public byte[] exportToPdf(ExpenseDTO.SearchFilter filter) {
        log.info("Début export PDF des dépenses");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Configuration du document PDF (paysage)
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate()); // Mode paysage

            // Marges
            document.setMargins(30, 30, 30, 30);

            // Polices
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // En-tête du document
            addPdfHeader(document, headerFont, filter);

            // Récupération des données
            List<Expense> expenses = getExpensesForExport(filter);

            // Tableau des dépenses
            addPdfExpenseTable(document, expenses, headerFont, regularFont);

            // Pied de page avec totaux
            addPdfFooter(document, expenses, headerFont, regularFont);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            log.info("Export PDF terminé - {} bytes générés", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Erreur lors de l'export PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Exporte les dépenses au format Excel
     */
    public byte[] exportToExcel(ExpenseDTO.SearchFilter filter) {
        log.info("Début export Excel des dépenses");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Dépenses SVS");

            // Styles Excel
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle dataStyle = createExcelDataStyle(workbook);
            CellStyle numberStyle = createExcelNumberStyle(workbook);
            CellStyle dateStyle = createExcelDateStyle(workbook);

            // En-tête Excel
            addExcelHeader(sheet, headerStyle);

            // Récupération des données
            List<Expense> expenses = getExpensesForExport(filter);

            // Données Excel
            addExcelData(sheet, expenses, dataStyle, numberStyle, dateStyle);

            // Auto-ajustement des colonnes
            autoSizeColumns(sheet);

            workbook.write(baos);

            byte[] excelBytes = baos.toByteArray();
            log.info("Export Excel terminé - {} bytes générés", excelBytes.length);
            return excelBytes;

        } catch (Exception e) {
            log.error("Erreur lors de l'export Excel", e);
            throw new RuntimeException("Erreur lors de la génération du fichier Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Génère le nom de fichier pour l'export
     */
    public String generateFileName(String format) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String code = String.valueOf(System.currentTimeMillis() % 10000);
        return String.format("svs-depenses-%s-%s.%s", dateStr, code, format.toLowerCase());
    }

    // ========== Méthodes privées PDF ==========

    private void addPdfHeader(Document document, PdfFont headerFont, ExpenseDTO.SearchFilter filter) {
        // Titre principal
        Paragraph title = new Paragraph("SVS - RAPPORT DES DÉPENSES MARITIMES")
                .setFont(headerFont)
                .setFontSize(18)
                .setFontColor(SVS_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        // Sous-titre avec entreprise
        Paragraph subtitle = new Paragraph("Dakar, Sénégal")
                .setFont(headerFont)
                .setFontSize(12)
                .setFontColor(SVS_HIGHLIGHT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subtitle);

        // Informations du rapport
        Paragraph reportInfo = new Paragraph()
                .setFont(headerFont)
                .setFontSize(10)
                .setMarginBottom(15);

        reportInfo.add("Date de génération: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (filter.getStartDate() != null || filter.getEndDate() != null) {
            reportInfo.add("\nPériode: ");
            if (filter.getStartDate() != null) {
                reportInfo.add("du " + filter.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            if (filter.getEndDate() != null) {
                reportInfo.add(" au " + filter.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        document.add(reportInfo);
    }

    private void addPdfExpenseTable(Document document, List<Expense> expenses, PdfFont headerFont, PdfFont regularFont) {
        // Création du tableau (7 colonnes)
        float[] columnWidths = {80f, 150f, 120f, 80f, 80f, 80f, 100f};
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes de colonnes
        addPdfTableHeader(table, headerFont);

        // Données
        boolean isOdd = false;
        for (Expense expense : expenses) {
            Color rowColor = isOdd ? SVS_SURFACE_100 : SVS_WHITE;
            addPdfTableRow(table, expense, regularFont, rowColor);
            isOdd = !isOdd;
        }

        document.add(table);
    }

    private void addPdfTableHeader(Table table, PdfFont headerFont) {
        String[] headers = {"Numéro", "Titre", "Fournisseur", "Montant XOF", "Montant EUR", "Date", "Mode Paiement"};

        for (String header : headers) {
            com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(SVS_PRIMARY)
                    .setFontColor(SVS_WHITE)
                    .setFont(headerFont)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            cell.add(new Paragraph(header));
            table.addHeaderCell(cell);
        }
    }

    private void addPdfTableRow(Table table, Expense expense, PdfFont regularFont, Color backgroundColor) {
        // Numéro
        table.addCell(createPdfCell(expense.getNumero(), regularFont, backgroundColor, TextAlignment.CENTER));

        // Titre
        String titre = expense.getTitre();
        if (titre.length() > 25) {
            titre = titre.substring(0, 22) + "...";
        }
        table.addCell(createPdfCell(titre, regularFont, backgroundColor, TextAlignment.LEFT));

        // Fournisseur
        String fournisseur = expense.getFournisseur() != null ? expense.getFournisseur().getNom() : "-";
        if (fournisseur.length() > 20) {
            fournisseur = fournisseur.substring(0, 17) + "...";
        }
        table.addCell(createPdfCell(fournisseur, regularFont, backgroundColor, TextAlignment.LEFT));

        // Montant XOF
        String montantXOF = expense.getMontantXOF() != null ?
                String.format("%,.0f FCFA", expense.getMontantXOF()) : "-";
        table.addCell(createPdfCell(montantXOF, regularFont, backgroundColor, TextAlignment.RIGHT));

        // Montant EUR
        String montantEUR = expense.getMontantEURO() != null ?
                String.format("%.2f €", expense.getMontantEURO()) : "-";
        table.addCell(createPdfCell(montantEUR, regularFont, backgroundColor, TextAlignment.RIGHT));

        // Date
        String date = expense.getDateDepense().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        table.addCell(createPdfCell(date, regularFont, backgroundColor, TextAlignment.CENTER));

        // Mode de paiement
        String paymentMethod = expense.getPaymentMethod() != null ? expense.getPaymentMethod().getNom() : "-";
        table.addCell(createPdfCell(paymentMethod, regularFont, backgroundColor, TextAlignment.LEFT));
    }

    private com.itextpdf.layout.element.Cell createPdfCell(String content, PdfFont font, Color backgroundColor, TextAlignment alignment) {
        return new com.itextpdf.layout.element.Cell()
                .setBackgroundColor(backgroundColor)
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(alignment)
                .setPadding(4)
                .add(new Paragraph(content));
    }

    private void addPdfFooter(Document document, List<Expense> expenses, PdfFont headerFont, PdfFont regularFont) {
        // Calcul des totaux
        BigDecimal totalXOF = expenses.stream()
                .filter(e -> e.getMontantXOF() != null)
                .map(Expense::getMontantXOF)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEUR = expenses.stream()
                .filter(e -> e.getMontantEURO() != null)
                .map(Expense::getMontantEURO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tableau des totaux
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{70f, 30f}));
        totalTable.setWidth(UnitValue.createPercentValue(100));
        totalTable.setMarginTop(20);

        // En-tête totaux
        com.itextpdf.layout.element.Cell totalHeader = new com.itextpdf.layout.element.Cell(1, 2)
                .setBackgroundColor(SVS_HIGHLIGHT)
                .setFontColor(SVS_WHITE)
                .setFont(headerFont)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
        totalHeader.add(new Paragraph("TOTAUX"));
        totalTable.addHeaderCell(totalHeader);

        // Total XOF
        totalTable.addCell(createPdfCell("Total en Francs CFA:", headerFont, SVS_SURFACE_100, TextAlignment.LEFT));
        totalTable.addCell(createPdfCell(String.format("%,.0f FCFA", totalXOF), headerFont, SVS_SURFACE_100, TextAlignment.RIGHT));

        // Total EUR
        totalTable.addCell(createPdfCell("Total en Euros:", headerFont, SVS_SURFACE_100, TextAlignment.LEFT));
        totalTable.addCell(createPdfCell(String.format("%.2f €", totalEUR), headerFont, SVS_SURFACE_100, TextAlignment.RIGHT));

        // Nombre de dépenses
        totalTable.addCell(createPdfCell("Nombre de dépenses:", headerFont, SVS_SURFACE_100, TextAlignment.LEFT));
        totalTable.addCell(createPdfCell(String.valueOf(expenses.size()), headerFont, SVS_SURFACE_100, TextAlignment.RIGHT));

        document.add(totalTable);
    }

    // ========== Méthodes privées Excel ==========

    private CellStyle createExcelHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Couleur de fond (SVS Primary)
        style.setFillForegroundColor(new XSSFColor(new byte[]{30, 64, (byte) 175}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Police
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // Bordures
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alignement
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createExcelDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // Police
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        // Bordures
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Alignement
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createExcelNumberStyle(XSSFWorkbook workbook) {
        CellStyle style = createExcelDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createExcelDateStyle(XSSFWorkbook workbook) {
        CellStyle style = createExcelDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void addExcelHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Numéro", "Titre", "Fournisseur", "Montant XOF", "Montant EUR", "Date", "Mode Paiement"};

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void addExcelData(Sheet sheet, List<Expense> expenses, CellStyle dataStyle, CellStyle numberStyle, CellStyle dateStyle) {
        int rowIndex = 1;

        for (Expense expense : expenses) {
            Row row = sheet.createRow(rowIndex++);

            // Numéro
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(expense.getNumero());
            cell0.setCellStyle(dataStyle);

            // Titre
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(expense.getTitre());
            cell1.setCellStyle(dataStyle);

            // Fournisseur
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(expense.getFournisseur() != null ? expense.getFournisseur().getNom() : "");
            cell2.setCellStyle(dataStyle);

            // Montant XOF
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            if (expense.getMontantXOF() != null) {
                cell3.setCellValue(expense.getMontantXOF().doubleValue());
            }
            cell3.setCellStyle(numberStyle);

            // Montant EUR
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            if (expense.getMontantEURO() != null) {
                cell4.setCellValue(expense.getMontantEURO().doubleValue());
            }
            cell4.setCellStyle(numberStyle);

            // Date
            org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
            cell5.setCellValue(expense.getDateDepense());
            cell5.setCellStyle(dateStyle);

            // Mode de paiement
            org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
            cell6.setCellValue(expense.getPaymentMethod() != null ? expense.getPaymentMethod().getNom() : "");
            cell6.setCellStyle(dataStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
            // Limite la largeur maximale
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }

    // ========== Méthodes utilitaires ==========

    private List<Expense> getExpensesForExport(ExpenseDTO.SearchFilter filter) {
        // Construction du tri (par date décroissante par défaut)
        Sort sort = Sort.by(Sort.Direction.DESC, "dateDepense");

        // Pagination large pour export (max 10000 éléments)
        Pageable pageable = PageRequest.of(0, 10000, sort);

        // Si filtres spécifiques
        if (hasFilters(filter)) {
            return expenseRepository.findWithFilters(
                    filter.getCategorieId(),
                    filter.getFournisseurId(),
                    filter.getStatut(),
                    filter.getPaymentMethodId(),
                    filter.getDevise(),
                    filter.getMinAmount(),
                    filter.getMaxAmount(),
                    filter.getStartDate(),
                    filter.getEndDate(),
                    pageable
            ).getContent();
        }

        // Toutes les dépenses actives
        return expenseRepository.findAll(pageable).getContent();
    }

    private boolean hasFilters(ExpenseDTO.SearchFilter filter) {
        return filter.getCategorieId() != null ||
                filter.getFournisseurId() != null ||
                filter.getStatut() != null ||
                filter.getPaymentMethodId() != null ||
                filter.getDevise() != null ||
                filter.getMinAmount() != null ||
                filter.getMaxAmount() != null ||
                filter.getStartDate() != null ||
                filter.getEndDate() != null;
    }
}