package sn.svs.backoffice.service;

import sn.svs.backoffice.domain.User;

public interface UserReloadService {

     User reloadUserWithRoles(User user);
     User reloadUserWithRoles(String username);
     User reloadUserWithRoles(Long userId);
}
