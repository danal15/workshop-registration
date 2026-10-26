package patmal.workshops.model.impl;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

public class WorkshopImpl implements Workshop {

    private final Integer id;
    private final String name;
    private final String description;
    private InstructorImpl instructor;

    public WorkshopImpl(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
    public String getDescription() {
        return description;
    }

    @Override
    public Instructor getInstructor() {
        return instructor;
    }

    // Sets both sides of the instructor-workshop link.
    public void assignInstructor(InstructorImpl instructor) {
        this.instructor = instructor;
        instructor.addWorkshop(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
