package com.github.nikolaikramskoy.tinkoff_translator_task_fall_2024;

import org.springframework.boot.SpringApplication;

public class TestTinkoffTranslatorTaskFall2024Application {

	public static void main(String[] args) {
		SpringApplication.from(TinkoffTranslatorTaskFall2024Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
