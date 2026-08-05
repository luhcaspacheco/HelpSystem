package com.helpsystem.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int COST = 12;

    private PasswordUtil() {
    }

    public static String gerarHash(String senhaPura) {
        if (senhaPura == null || senhaPura.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        }
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(COST));
    }

    public static boolean conferir(String senhaPura, String hashSalvo) {
        if (senhaPura == null || hashSalvo == null || hashSalvo.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(senhaPura, hashSalvo);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
