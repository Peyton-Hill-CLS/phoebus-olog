package org.phoebus.olog.security;

import org.phoebus.olog.WebSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import java.util.HashMap;
import java.util.List;

@Configuration
public class NameUtil {

    @Autowired
    private LdapTemplate ldapTemplate;

    // name cache to improve log creation speed.
    private HashMap<String, String> nameCache = new HashMap<>();

    public synchronized String findPersonByUsername(String username) {
        String base = ""; // Relative to spring.ldap.base
        String filter = "(&(objectClass=user)(samAccountName=" + username + "))";

        if(nameCache.containsKey(username)) {
            return nameCache.get(username);
        }

        try {
            List<String> firstName = ldapTemplate.search(base, filter, (AttributesMapper<String>) attrs -> (String) attrs.get("givenName").get());
            List<String> lastName = ldapTemplate.search(base, filter, (AttributesMapper<String>) attrs -> (String) attrs.get("sn").get());

            if (firstName.isEmpty() || lastName.isEmpty())  {
                return username;
            }

            String name = firstName.get(0) + " " + lastName.get(0);
            nameCache.put(username, name);
            return name;
        } catch (Exception e) {
            return username;
        }
    }
}
