package patmal.workshops.model.impl;

import java.util.ArrayList;
import java.util.List;

import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

public class TeamImpl implements Team {

    private final Integer id;
    private final String name;
    private final WorkshopImpl workshop;
    private final List<StudentImpl> members = new ArrayList<>();

    public TeamImpl(Integer id, String name, WorkshopImpl workshop) {
        this.id = id;
        this.name = name;
        this.workshop = workshop;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(members);
    }

    @Override
    public Workshop getWorkshop() {
        return workshop;
    }

    // The only place team membership is written. Sets both sides of the link.
    public void addStudent(StudentImpl student) {
        if (!members.contains(student)) {
            members.add(student);
            student.setTeam(this);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
