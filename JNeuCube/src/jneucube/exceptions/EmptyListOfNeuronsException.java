/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jneucube.exceptions;

/**
 *
 * @author em9403
 */
public class EmptyListOfNeuronsException extends RuntimeException{
    public EmptyListOfNeuronsException(String message){
        super(message);
    }
}
