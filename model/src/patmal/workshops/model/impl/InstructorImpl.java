package patmal.workshops.model.impl;

import java.util.ArrayList;
import java.util.List;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;

public class InstructorImpl extends PersonImpl implements Instructor {

    private final List<WorkshopImpl> workshops = new ArrayList<>();

    public InstructorImpl(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops);
    }

    // Package private so only WorkshopImpl.assignInstructor can set it.
    void addWorkshop(WorkshopImpl workshop) {
        if (!workshops.contains(workshop)) {
            workshops.add(workshop);
        }
    }
}
