package com.worth.ifs.application.repository;

import com.worth.ifs.workflow.domain.Process;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by nunoalexandre on 15/09/15.
 */
public interface ProcessRepository extends PagingAndSortingRepository<Process, Long> {
    Process findById(@Param("id") Long id);
    List<Process> findAll();
}



