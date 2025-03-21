package com.br.samples.testAppTaskManager.config;

import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.annotations.OblivionWire;
import com.br.samples.testAppTaskManager.repository.TaskRepository;
import com.br.samples.testAppTaskManager.service.TaskService;

@OblivionService
public class OblivionConfig {

  @OblivionWire private TaskService taskService;

  @OblivionWire private TaskRepository taskRepository;
}
