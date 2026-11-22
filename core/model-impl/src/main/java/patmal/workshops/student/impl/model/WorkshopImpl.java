package patmal.workshops.student.impl.model;

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

    // Sets both sides of the instructor-workshop link. If the workshop moves to another
    // instructor, it is first removed from the previous one, so both sides stay consistent.
    public void assignInstructor(InstructorImpl instructor) {
        if (this.instructor != null) {
            this.instructor.removeWorkshop(this);
        }
        this.instructor = instructor;
        instructor.addWorkshop(this);
    }

    // Identity is the system-assigned id.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return id.equals(((WorkshopImpl) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
