package com.georgecalm.sprockets;

public class UndefinedConstantException extends Exception {
    private static final long serialVersionUID = 3117364949371904600L;
    
    public UndefinedConstantException(String message) {
        super(message);
    }
}
