/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.util;

/**
 *
 * @author Josafath Israel Espinosa Ramos
 */
public class NeuCubeRuntimeException extends RuntimeException {

    public NeuCubeRuntimeException() {
        super();
    }

    public NeuCubeRuntimeException(String message) {
        super(message);
    }

    public NeuCubeRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeuCubeRuntimeException(Throwable cause) {
        super(cause);
    }
}
