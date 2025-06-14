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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sn.svs.backoffice.domain.Invoice;
import sn.svs.backoffice.domain.InvoiceLineItem;
import sn.svs.backoffice.dto.InvoiceDTO;
import sn.svs.backoffice.repository.InvoiceRepository;
import sn.svs.backoffice.specification.InvoiceSpecification;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'export des factures en PDF et Excel
 * Génère aussi les factures individuelles au format professionnel
 * SVS - Dakar, Sénégal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceExportService {

    private final InvoiceRepository invoiceRepository;

    @Value("${app.export.storage-path:/tmp/exports}")
    private String exportStoragePath;

    @Value("${app.company.name:SVS Maritime Services}")
    private String companyName;

    @Value("${app.company.address:Port Autonome de Dakar, Sénégal}")
    private String companyAddress;

    @Value("${app.company.phone:+221 33 123 45 67}")
    private String companyPhone;

    @Value("${app.company.email:contact@svs.sn}")
    private String companyEmail;

    @Value("${app.company.ninea:123456789}")
    private String companyNinea;

    @Value("${app.company.rccm:SN-DKR-2023-B-12345}")
    private String companyRccm;

    // Couleurs du thème SVS
    private static final Color SVS_PRIMARY = new DeviceRgb(30, 64, 175);        // #1e40af
    private static final Color SVS_HIGHLIGHT = new DeviceRgb(6, 182, 212);      // #06b6d4
    private static final Color SVS_SURFACE_100 = new DeviceRgb(241, 245, 249);  // #f1f5f9
    private static final Color SVS_SURFACE_200 = new DeviceRgb(226, 232, 240);  // #e2e8f0
    private static final Color SVS_WHITE = new DeviceRgb(255, 255, 255);        // #ffffff

    // ========== EXPORT LISTE FACTURES ==========

    /**
     * Exporte la liste des factures au format PDF
     */
    public byte[] exportListToPdf(InvoiceDTO.SearchFilter filter) {
        log.info("Début export PDF liste des factures");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate()); // Mode paysage

            document.setMargins(30, 30, 30, 30);

            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // En-tête du document
            addListPdfHeader(document, headerFont, filter);

            // Récupération des données
            List<Invoice> invoices = getInvoicesForExport(filter);

            // Tableau des factures
            addListPdfInvoiceTable(document, invoices, headerFont, regularFont);

            // Pied de page avec totaux
            addListPdfFooter(document, invoices, headerFont, regularFont);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            log.info("Export PDF liste terminé - {} bytes générés", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Erreur lors de l'export PDF liste", e);
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Exporte la liste des factures au format Excel
     */
    public byte[] exportListToExcel(InvoiceDTO.SearchFilter filter) {
        log.info("Début export Excel liste des factures");

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Factures SVS");

            // Styles Excel
            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle dataStyle = createExcelDataStyle(workbook);
            CellStyle numberStyle = createExcelNumberStyle(workbook);
            CellStyle dateStyle = createExcelDateStyle(workbook);

            // En-tête Excel
            addListExcelHeader(sheet, headerStyle);

            // Récupération des données
            List<Invoice> invoices = getInvoicesForExport(filter);

            // Données Excel
            addListExcelData(sheet, invoices, dataStyle, numberStyle, dateStyle);

            // Auto-ajustement des colonnes
            autoSizeColumns(sheet, 8);

            workbook.write(baos);

            byte[] excelBytes = baos.toByteArray();
            log.info("Export Excel liste terminé - {} bytes générés", excelBytes.length);
            return excelBytes;

        } catch (Exception e) {
            log.error("Erreur lors de l'export Excel liste", e);
            throw new RuntimeException("Erreur lors de la génération du fichier Excel: " + e.getMessage(), e);
        }
    }

    // ========== EXPORT FACTURE INDIVIDUELLE ==========

    /**
     * Génère une facture individuelle au format PDF professionnel
     */
    public byte[] exportInvoiceToPdf(Long invoiceId) {
        log.info("Génération PDF facture individuelle ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findByIdWithFetch(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée: " + invoiceId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4); // Format portrait

            document.setMargins(40, 40, 40, 40);

            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // En-tête entreprise + titre facture
            addInvoicePdfHeader(document, invoice, headerFont, regularFont);

            // Informations client et navire
            addInvoicePdfClientInfo(document, invoice, headerFont, regularFont);

            // Tableau des prestations
            addInvoicePdfServiceTable(document, invoice, headerFont, regularFont);

            // Totaux et conditions
            addInvoicePdfFooter(document, invoice, headerFont, regularFont);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            log.info("Génération PDF facture terminée - {} bytes générés", pdfBytes.length);
            return pdfBytes;

        } catch (Exception e) {
            log.error("Erreur lors de la génération PDF facture", e);
            throw new RuntimeException("Erreur lors de la génération du PDF facture: " + e.getMessage(), e);
        }
    }

    // ========== MÉTHODES PRIVÉES - LISTE PDF ==========

    private void addListPdfHeader(Document document, PdfFont headerFont, InvoiceDTO.SearchFilter filter) {
        // Titre principal
        Paragraph title = new Paragraph("SVS - RAPPORT DES FACTURES")
                .setFont(headerFont)
                .setFontSize(18)
                .setFontColor(SVS_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        // Sous-titre
        Paragraph subtitle = new Paragraph("Services Portuaires - Dakar, Sénégal")
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

        if (filter.getDateDebut() != null || filter.getDateFin() != null) {
            reportInfo.add("\nPériode: ");
            if (filter.getDateDebut() != null) {
                reportInfo.add("du " + filter.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            if (filter.getDateFin() != null) {
                reportInfo.add(" au " + filter.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        if (filter.getStatut() != null) {
            reportInfo.add("\nStatut: " + filter.getStatut().getLabel());
        }

        document.add(reportInfo);
    }

    private void addListPdfInvoiceTable(Document document, List<Invoice> invoices, PdfFont headerFont, PdfFont regularFont) {
        // Création du tableau (8 colonnes)
        float[] columnWidths = {80f, 120f, 100f, 80f, 80f, 80f, 80f, 80f};
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes de colonnes
        addListPdfTableHeader(table, headerFont);

        // Données
        boolean isOdd = false;
        for (Invoice invoice : invoices) {
            Color rowColor = isOdd ? SVS_SURFACE_100 : SVS_WHITE;
            addListPdfTableRow(table, invoice, regularFont, rowColor);
            isOdd = !isOdd;
        }

        document.add(table);
    }

    private void addListPdfTableHeader(Table table, PdfFont headerFont) {
        String[] headers = {"Numéro", "Compagnie", "Navire", "Date", "Échéance", "Montant XOF", "Montant EUR", "Statut"};

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

    private void addListPdfTableRow(Table table, Invoice invoice, PdfFont regularFont, Color backgroundColor) {
        // Numéro
        table.addCell(createPdfCell(invoice.getNumero(), regularFont, backgroundColor, TextAlignment.CENTER));

        // Compagnie
        String compagnie = invoice.getCompagnie() != null ? invoice.getCompagnie().getNom() : "-";
        if (compagnie.length() > 18) {
            compagnie = compagnie.substring(0, 15) + "...";
        }
        table.addCell(createPdfCell(compagnie, regularFont, backgroundColor, TextAlignment.LEFT));

        // Navire
        String navire = invoice.getNavire() != null ? invoice.getNavire().getNom() : "-";
        if (navire.length() > 15) {
            navire = navire.substring(0, 12) + "...";
        }
        table.addCell(createPdfCell(navire, regularFont, backgroundColor, TextAlignment.LEFT));

        // Date facture
        String dateFacture = invoice.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        table.addCell(createPdfCell(dateFacture, regularFont, backgroundColor, TextAlignment.CENTER));

        // Date échéance
        String dateEcheance = invoice.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        table.addCell(createPdfCell(dateEcheance, regularFont, backgroundColor, TextAlignment.CENTER));

        // Montant XOF
        String montantXOF = String.format("%,.0f FCFA", invoice.getMontantTotal());
        table.addCell(createPdfCell(montantXOF, regularFont, backgroundColor, TextAlignment.RIGHT));

        // Montant EUR (calculé depuis les prestations)
        BigDecimal totalEUR = calculateTotalEuro(invoice);
        String montantEUR = totalEUR.compareTo(BigDecimal.ZERO) > 0 ?
                String.format("%.2f €", totalEUR) : "-";
        table.addCell(createPdfCell(montantEUR, regularFont, backgroundColor, TextAlignment.RIGHT));

        // Statut
        table.addCell(createPdfCell(invoice.getStatut().getLabel(), regularFont, backgroundColor, TextAlignment.CENTER));
    }

    private void addListPdfFooter(Document document, List<Invoice> invoices, PdfFont headerFont, PdfFont regularFont) {
        // Calcul des totaux
        BigDecimal totalXOF = invoices.stream()
                .map(Invoice::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEUR = invoices.stream()
                .map(this::calculateTotalEuro)
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

        // Nombre de factures
        totalTable.addCell(createPdfCell("Nombre de factures:", headerFont, SVS_SURFACE_100, TextAlignment.LEFT));
        totalTable.addCell(createPdfCell(String.valueOf(invoices.size()), headerFont, SVS_SURFACE_100, TextAlignment.RIGHT));

        document.add(totalTable);
    }

    // ========== MÉTHODES PRIVÉES - FACTURE INDIVIDUELLE PDF ==========

    private void addInvoicePdfHeader(Document document, Invoice invoice, PdfFont headerFont, PdfFont regularFont) {
        // Tableau en-tête avec logo et titre facture
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60f, 40f}));
        headerTable.setWidth(UnitValue.createPercentValue(100));

        // Informations entreprise (gauche)
        com.itextpdf.layout.element.Cell companyCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null);

        Paragraph companyTitle = new Paragraph(companyName.toUpperCase())
                .setFont(headerFont)
                .setFontSize(16)
                .setFontColor(SVS_PRIMARY);
        companyCell.add(companyTitle);

        companyCell.add(new Paragraph(companyAddress).setFont(regularFont).setFontSize(10));
        companyCell.add(new Paragraph("Tél: " + companyPhone + " / " + companyEmail).setFont(regularFont).setFontSize(10));
        companyCell.add(new Paragraph("NINEA: " + companyNinea).setFont(regularFont).setFontSize(10));
        companyCell.add(new Paragraph("RCCM: " + companyRccm).setFont(regularFont).setFontSize(10));

        headerTable.addCell(companyCell);

        // Titre FACTURE et numéro (droite)
        com.itextpdf.layout.element.Cell invoiceCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT);

        Paragraph factureTitle = new Paragraph("FACTURE")
                .setFont(headerFont)
                .setFontSize(24)
                .setFontColor(SVS_PRIMARY);
        invoiceCell.add(factureTitle);

        // Numéro avec fond coloré
        com.itextpdf.layout.element.Cell numeroCell = new com.itextpdf.layout.element.Cell()
                .setBackgroundColor(SVS_HIGHLIGHT)
                .setFontColor(SVS_WHITE)
                .setFont(headerFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
        numeroCell.add(new Paragraph("N° " + invoice.getNumero()));

        Table numeroTable = new Table(1);
        numeroTable.addCell(numeroCell);
        invoiceCell.add(numeroTable);

        headerTable.addCell(invoiceCell);
        document.add(headerTable);

        document.add(new Paragraph("\n"));
    }

    private void addInvoicePdfClientInfo(Document document, Invoice invoice, PdfFont headerFont, PdfFont regularFont) {
        // Tableau avec informations client et navire
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50f, 25f, 25f}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        // Facturé à (gauche)
        com.itextpdf.layout.element.Cell clientCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null);
        clientCell.add(new Paragraph("Facturé à:").setFont(headerFont).setFontColor(SVS_PRIMARY));
        clientCell.add(new Paragraph(invoice.getCompagnie().getNom()).setFont(headerFont).setFontSize(12));

        if (invoice.getCompagnie().getRaisonSociale() != null) {
            clientCell.add(new Paragraph(invoice.getCompagnie().getRaisonSociale()).setFont(regularFont).setFontSize(10));
        }
        if (invoice.getCompagnie().getAdresse() != null) {
            clientCell.add(new Paragraph(invoice.getCompagnie().getAdresse()).setFont(regularFont).setFontSize(10));
        }
        if (invoice.getCompagnie().getPays() != null) {
            clientCell.add(new Paragraph(invoice.getCompagnie().getPays()).setFont(regularFont).setFontSize(10));
        }
        if (invoice.getCompagnie().getTelephone() != null) {
            clientCell.add(new Paragraph("Tél: " + invoice.getCompagnie().getTelephone()).setFont(regularFont).setFontSize(10));
        }
        if (invoice.getCompagnie().getNinea() != null) {
            clientCell.add(new Paragraph("NINEA: " + invoice.getCompagnie().getNinea()).setFont(regularFont).setFontSize(10));
        }

        infoTable.addCell(clientCell);

        // Navire concerné (centre)
        com.itextpdf.layout.element.Cell shipCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null);
        shipCell.add(new Paragraph("Navire concerné:").setFont(headerFont).setFontColor(SVS_PRIMARY));
        shipCell.add(new Paragraph(invoice.getNavire().getNom()).setFont(headerFont).setFontSize(12));

        if (invoice.getNavire().getNumeroIMO() != null) {
            shipCell.add(new Paragraph("IMO: " + invoice.getNavire().getNumeroIMO()).setFont(regularFont).setFontSize(10));
        }
        if (invoice.getNavire().getTypeNavire() != null) {
            shipCell.add(new Paragraph("Type: " + invoice.getNavire().getTypeNavire()).setFont(regularFont).setFontSize(10));
        }

        infoTable.addCell(shipCell);

        // Infos de facturation (droite)
        com.itextpdf.layout.element.Cell dateCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null);
        dateCell.add(new Paragraph("Infos de facturation:").setFont(headerFont).setFontColor(SVS_PRIMARY));
        dateCell.add(new Paragraph("Date de facture: " +
                invoice.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(regularFont).setFontSize(10));
        dateCell.add(new Paragraph("Date d'échéance: " +
                invoice.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(regularFont).setFontSize(10));

        infoTable.addCell(dateCell);
        document.add(infoTable);

        document.add(new Paragraph("\n"));
    }

    private void addInvoicePdfServiceTable(Document document, Invoice invoice, PdfFont headerFont, PdfFont regularFont) {
        document.add(new Paragraph("Détail des prestations")
                .setFont(headerFont)
                .setFontSize(14)
                .setFontColor(SVS_PRIMARY));

        // Tableau des prestations
        float[] columnWidths = {200f, 40f, 80f, 80f, 80f};
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        String[] headers = {"Description", "Qté", "Prix unitaire", "Total HT (XOF)", "Total HT (EUR)"};
        for (String header : headers) {
            com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(SVS_SURFACE_200)
                    .setFont(headerFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            cell.add(new Paragraph(header));
            table.addHeaderCell(cell);
        }

        // Lignes de prestations
        for (InvoiceLineItem item : invoice.getPrestations()) {
            // Description avec sous-description
            com.itextpdf.layout.element.Cell descCell = new com.itextpdf.layout.element.Cell()
                    .setFont(regularFont)
                    .setFontSize(10)
                    .setPadding(8);

            Paragraph mainDesc = new Paragraph(item.getDescription())
                    .setFont(headerFont)
                    .setFontSize(10);
            descCell.add(mainDesc);

            if (item.getOperation() != null && item.getOperation().getDescription() != null) {
                Paragraph subDesc = new Paragraph(item.getOperation().getDescription())
                        .setFont(regularFont)
                        .setFontSize(9)
                        .setFontColor(new DeviceRgb(100, 100, 100));
                descCell.add(subDesc);
            }
            table.addCell(descCell);

            // Quantité
            table.addCell(createPdfCell(item.getQuantite().toString(), regularFont, SVS_WHITE, TextAlignment.CENTER));

            // Prix unitaire
            String prixUnitaire = String.format("%,.0f FCFA", item.getPrixUnitaireXOF());
            table.addCell(createPdfCell(prixUnitaire, regularFont, SVS_WHITE, TextAlignment.RIGHT));

            // Total XOF
            String totalXOF = String.format("%,.0f FCFA", item.getMontantXOF());
            table.addCell(createPdfCell(totalXOF, regularFont, SVS_WHITE, TextAlignment.RIGHT));

            // Total EUR
            String totalEUR = item.getMontantEURO() != null ?
                    String.format("%.2f €", item.getMontantEURO()) : "-";
            table.addCell(createPdfCell(totalEUR, regularFont, SVS_WHITE, TextAlignment.RIGHT));
        }

        document.add(table);

        // Totaux
        addInvoicePdfTotals(document, invoice, headerFont, regularFont);
    }

    private void addInvoicePdfTotals(Document document, Invoice invoice, PdfFont headerFont, PdfFont regularFont) {
        // Tableau des totaux (aligné à droite)
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{70f, 30f}));
        totalTable.setWidth(UnitValue.createPercentValue(100));
        totalTable.setMarginTop(20);

        // Sous-total
        totalTable.addCell(createPdfCell("Sous-total HT:", regularFont, SVS_WHITE, TextAlignment.RIGHT));
        totalTable.addCell(createPdfCell(String.format("%,.0f FCFA", invoice.getSousTotal()),
                headerFont, SVS_WHITE, TextAlignment.RIGHT));

        // TVA
        totalTable.addCell(createPdfCell("TVA (" + invoice.getTauxTva() + "%):", regularFont, SVS_WHITE, TextAlignment.RIGHT));
        totalTable.addCell(createPdfCell(String.format("%,.0f FCFA", invoice.getTva()),
                regularFont, SVS_WHITE, TextAlignment.RIGHT));

        // Total TTC
        com.itextpdf.layout.element.Cell totalLabelCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null)
                .setFont(headerFont)
                .setFontSize(14)
                .setFontColor(SVS_PRIMARY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8);
        totalLabelCell.add(new Paragraph("Total TTC:"));
        totalTable.addCell(totalLabelCell);

        com.itextpdf.layout.element.Cell totalValueCell = new com.itextpdf.layout.element.Cell()
                .setBackgroundColor(SVS_HIGHLIGHT)
                .setFontColor(SVS_WHITE)
                .setFont(headerFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(8);
        totalValueCell.add(new Paragraph(String.format("%,.0f FCFA", invoice.getMontantTotal())));
        totalTable.addCell(totalValueCell);

        document.add(totalTable);
    }

    private void addInvoicePdfFooter(Document document, Invoice invoice, PdfFont headerFont, PdfFont regularFont) {
        document.add(new Paragraph("\n"));

        // Tableau pour notes et conditions
        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{50f, 50f}));
        footerTable.setWidth(UnitValue.createPercentValue(100));

        // Notes (gauche)
        com.itextpdf.layout.element.Cell notesCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null);
        notesCell.add(new Paragraph("Notes:").setFont(headerFont).setFontColor(SVS_PRIMARY));

        String notes = invoice.getNotes() != null ? invoice.getNotes() : "Facture pour services portuaires";
        if (invoice.getDateFacture() != null) {
            notes += " - Escale du " + invoice.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        notesCell.add(new Paragraph(notes).setFont(regularFont).setFontSize(10));

        footerTable.addCell(notesCell);

        // Conditions de paiement (droite)
        com.itextpdf.layout.element.Cell conditionsCell = new com.itextpdf.layout.element.Cell()
                .setBorder(null);
        conditionsCell.add(new Paragraph("Conditions de paiement:").setFont(headerFont).setFontColor(SVS_PRIMARY));

        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(invoice.getDateFacture(), invoice.getDateEcheance());
        conditionsCell.add(new Paragraph("Paiement à " + daysDiff + " jours à compter de la date de facture. " +
                "Pénalités de retard : 1.5% par mois. En cas de retard de paiement, une pénalité " +
                "forfaitaire sera appliquée.").setFont(regularFont).setFontSize(10));

        footerTable.addCell(conditionsCell);
        document.add(footerTable);

        // Informations bancaires
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Informations bancaires:")
                .setFont(headerFont)
                .setFontColor(SVS_PRIMARY)
                .setFontSize(12));

        Paragraph bankInfo = new Paragraph()
                .setFont(regularFont)
                .setFontSize(10);
        bankInfo.add("Banque: Banque De Dakar\n");
        bankInfo.add("IBAN: SN08 BK00 0000 0000 0000 0000\n");
        bankInfo.add("BIC: BKSNSNDA");

        document.add(bankInfo);

        // Signature
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("Signature et cachet de l'entreprise")
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    // ========== MÉTHODES PRIVÉES - EXCEL ==========

    private void addListExcelHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Numéro", "Compagnie", "Navire", "Date Facture", "Date Échéance", "Montant XOF", "Montant EUR", "Statut"};

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void addListExcelData(Sheet sheet, List<Invoice> invoices, CellStyle dataStyle, CellStyle numberStyle, CellStyle dateStyle) {
        int rowIndex = 1;

        for (Invoice invoice : invoices) {
            Row row = sheet.createRow(rowIndex++);

            // Numéro
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(invoice.getNumero());
            cell0.setCellStyle(dataStyle);

            // Compagnie
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(invoice.getCompagnie() != null ? invoice.getCompagnie().getNom() : "");
            cell1.setCellStyle(dataStyle);

            // Navire
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(invoice.getNavire() != null ? invoice.getNavire().getNom() : "");
            cell2.setCellStyle(dataStyle);

            // Date facture
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            cell3.setCellValue(invoice.getDateFacture());
            cell3.setCellStyle(dateStyle);

            // Date échéance
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            cell4.setCellValue(invoice.getDateEcheance());
            cell4.setCellStyle(dateStyle);

            // Montant XOF
            org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
            cell5.setCellValue(invoice.getMontantTotal().doubleValue());
            cell5.setCellStyle(numberStyle);

            // Montant EUR
            org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
            BigDecimal totalEUR = calculateTotalEuro(invoice);
            if (totalEUR.compareTo(BigDecimal.ZERO) > 0) {
                cell6.setCellValue(totalEUR.doubleValue());
            }
            cell6.setCellStyle(numberStyle);

            // Statut
            org.apache.poi.ss.usermodel.Cell cell7 = row.createCell(7);
            cell7.setCellValue(invoice.getStatut().getLabel());
            cell7.setCellStyle(dataStyle);
        }
    }

    // ========== MÉTHODES UTILITAIRES - STYLES ==========

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

    private com.itextpdf.layout.element.Cell createPdfCell(String content, PdfFont font, Color backgroundColor, TextAlignment alignment) {
        return new com.itextpdf.layout.element.Cell()
                .setBackgroundColor(backgroundColor)
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(alignment)
                .setPadding(4)
                .add(new Paragraph(content));
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // Limite la largeur maximale
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Calcule le montant total en euros d'une facture
     */
    private BigDecimal calculateTotalEuro(Invoice invoice) {
        if (invoice.getPrestations() == null || invoice.getPrestations().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return invoice.getPrestations().stream()
                .filter(item -> item.getMontantEURO() != null)
                .map(InvoiceLineItem::getMontantEURO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Récupère les factures pour export selon les filtres
     */
    private List<Invoice> getInvoicesForExport(InvoiceDTO.SearchFilter filter) {
        // Construction du tri (par date décroissante par défaut)
        Sort sort = Sort.by(Sort.Direction.DESC, "dateFacture");

        // Pagination large pour export (max 10000 éléments)
        Pageable pageable = PageRequest.of(0, 10000, sort);

        // Si filtres spécifiques
        if (hasFilters(filter)) {
            // Utilisation des Specifications pour requête dynamique
            Specification<Invoice> specification = InvoiceSpecification.withFilters(filter);
            log.debug("Exécution de la requête avec specification");
            return invoiceRepository.findAll(specification, pageable).getContent();
        }

        // Toutes les factures actives
        return invoiceRepository.findAll(pageable).getContent();
    }

    /**
     * Vérifie si des filtres sont appliqués
     */
    private boolean hasFilters(InvoiceDTO.SearchFilter filter) {
        return filter.getCompagnieId() != null ||
                filter.getNavireId() != null ||
                filter.getStatut() != null ||
                filter.getDateDebut() != null ||
                filter.getDateFin() != null ||
                filter.getMois() != null ||
                filter.getAnnee() != null ||
                (filter.getSearch() != null && !filter.getSearch().trim().isEmpty());
    }

    /**
     * Génère le nom de fichier pour l'export
     */
    public String generateFileName(String format, String type) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String code = String.valueOf(System.currentTimeMillis() % 10000);

        String prefix = switch (type) {
            case "list" -> "svs-factures-liste";
            case "invoice" -> "svs-facture";
            case "monthly" -> "svs-factures-mensuel";
            default -> "svs-factures";
        };

        return String.format("%s-%s-%s.%s", prefix, dateStr, code, format.toLowerCase());
    }

    /**
     * Génère un rapport mensuel des factures
     */
    public byte[] generateMonthlyReport(int year, int month) {
        log.info("Génération rapport mensuel: {}/{}", month, year);

        // Créer un filtre pour le mois spécifique
        InvoiceDTO.SearchFilter filter = InvoiceDTO.SearchFilter.builder()
                .annee(year)
                .mois(month)
                .build();

        return exportListToPdf(filter);
    }

    /**
     * Génère un état des comptes clients
     */
    public byte[] generateAccountStatement(Long compagnieId, LocalDate asOfDate) {
        log.info("Génération état des comptes - Compagnie: {}, Date: {}", compagnieId, asOfDate);

        LocalDate endDate = asOfDate != null ? asOfDate : LocalDate.now();
        LocalDate startDate = endDate.minusYears(1); // 1 an de données

        InvoiceDTO.SearchFilter filter = InvoiceDTO.SearchFilter.builder()
                .compagnieId(compagnieId)
                .dateDebut(startDate)
                .dateFin(endDate)
                .build();

        return exportListToPdf(filter);
    }
}
