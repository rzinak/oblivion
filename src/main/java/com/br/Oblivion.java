package com.br;

import com.br.oblivion.annotations.OblivionWire;
import com.br.oblivion.util.OblivionSetup;
import com.br.samples.service.TaskService;
import com.br.samples.service.UserService;

public class Oblivion {

  @OblivionWire UserService userService;
  @OblivionWire TaskService taskService;

  public static void main(String[] args) throws Exception {
    try {
      OblivionSetup.init();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public void testing() {
    // System.out.println("TASK SERVICE: " + taskService);
    // taskService.setTaskName("Code PROTO 1");
    // taskService.setIsAvailable(false);
    // System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getTaskName());
    // System.out.println("CURRENT TASK SERVICE BOOL VAL: " + taskService.getIsAvailable());
    // System.out.println("CURRENT TASK SERVICE BOXED BOOL VAL: " + taskService.getIsRegistered());
    // System.out.println("CURRENT TASK SERVICE NORMAL INT VAL: " + taskService.getNormalInt());
    // System.out.println("CURRENT TASK SERVICE BOXED INT VAL: " + taskService.getBoxedInt());
    // System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getStringText());
    // System.out.println("CURRENT TASK SERVICE LIST VAL: " + taskService.getListString());
    // System.out.println("CURRENT TASK SERVICE MAP VAL: " + taskService.getMapStringString());
  }
}
