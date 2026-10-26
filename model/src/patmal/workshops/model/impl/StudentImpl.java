package patmal.workshops.model.impl;

import java.util.ArrayList;
import java.util.List;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;

public class StudentImpl extends PersonImpl implements Student {

    private TeamImpl team;

    public StudentImpl(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getTeamName() {
        return team == null ? null : team.getName();
    }

    @Override
    public Instructor getInstructor() {
        return team == null ? null : team.getWorkshop().getInstructor();
    }

    @Override
    public List<Student> getTeamMates() {
        if (team == null) {
            return new ArrayList<>();
        }
        List<Student> mates = new ArrayList<>(team.getStudents());
        mates.remove(this);
        return mates;
    }

    public boolean hasTeam() {
        return team != null;
    }

    // Package private so only TeamImpl.addStudent can set it, keeping both sides together.
    void setTeam(TeamImpl team) {
        this.team = team;
    }
}
