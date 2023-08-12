package org.example.dao.api;

import org.example.dao.entities.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ITaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

}
