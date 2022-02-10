package com.echobaba.milky.example.domain.student.repository;

import com.echobaba.milky.domain.support.repository.DomainRepository;
import com.echobaba.milky.domain.support.repository.DomainRepositoryService;
import com.echobaba.milky.example.domain.student.Student;

@DomainRepository(Student.class)
public class StudentRepository implements DomainRepositoryService<Student> {

    @Override
    public Student getByAggregateId(String aggregateId) {
        return new Student(Long.valueOf(aggregateId), "jack");
    }

    @Override
    public void save(Student student) {
        System.out.println(student);
    }

}
