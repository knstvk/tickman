//package com.haulmont.tickman.security;
//
//import io.jmix.core.security.UserRepository;
//import org.springframework.context.annotation.Primary;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.util.Collections;
//import java.util.List;
//
//@Primary
//@Component
//public class SimpleUserRepository implements UserRepository {
//
//    private SimpleUser systemUser;
//    private SimpleUser anonymousUser;
//
//    @Override
//    public UserDetails getSystemUser() {
//        return systemUser;
//    }
//
//    @Override
//    public UserDetails getAnonymousUser() {
//        return anonymousUser;
//    }
//
//    @Override
//    public List<? extends UserDetails> getByUsernameLike(String substring) {
//        return Collections.singletonList(systemUser);
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return systemUser;
//    }
//
//    @PostConstruct
//    private void init() {
//        systemUser = createSystemUser();
//        anonymousUser = createAnonymousUser();
//    }
//
//    private SimpleUser createAnonymousUser() {
//        return new SimpleUser("system", "", Collections.emptyList());
//    }
//
//    private SimpleUser createSystemUser() {
//        return new SimpleUser("anonymous", "", Collections.emptyList());
//    }
//
//}
