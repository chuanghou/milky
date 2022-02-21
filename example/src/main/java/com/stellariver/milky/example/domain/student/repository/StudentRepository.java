package com.stellariver.milky.example.domain.student.repository;

import com.stellariver.milky.domain.support.context.Context;
import com.stellariver.milky.domain.support.repository.DomainRepositoryService;
import com.stellariver.milky.example.domain.student.Student;

public class StudentRepository implements DomainRepositoryService<Student> {

    @Override
    public Student getByAggregateId(String aggregateId, Context context) {
        return new Student(Long.valueOf(aggregateId), "jack");
    }

    @Override
    public void save(Student student, Context context) {
        System.out.println(student);
    }

}
