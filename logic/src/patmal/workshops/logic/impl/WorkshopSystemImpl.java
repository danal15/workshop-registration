package patmal.workshops.logic.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.model.impl.InstructorImpl;
import patmal.workshops.model.impl.StudentImpl;
import patmal.workshops.model.impl.TeamImpl;
import patmal.workshops.model.impl.WorkshopImpl;

/**
 * Unknown keys: lookups never throw. getX(id) returns null and getStudentsOfX(id)
 * returns an empty list for an id that does not exist. Exceptions are used only for
 * operations that would corrupt the system, and are thrown before anything changes.
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    private final Map<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final Map<String, StudentImpl> students = new LinkedHashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final Map<Integer, TeamImpl> teams = new LinkedHashMap<>();

    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    public WorkshopSystemImpl() {
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (instructors.containsKey(id)) {
            throw new IllegalArgumentException("instructor id already in use: " + id);
        }
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        return instructor;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(nextWorkshopId, workshop);
        nextWorkshopId++;
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl known = require(instructor);
        WorkshopImpl target = require(workshop);
        target.assignInstructor(known);
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) {
            throw new IllegalArgumentException("student id already in use: " + id);
        }
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        return student;
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        WorkshopImpl target = require(workshop);
        if (members == null) {
            throw new IllegalArgumentException("a team needs a member list");
        }

        // Validate everything first, so a rejected call changes nothing and consumes no id.
        List<StudentImpl> joining = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String memberId : members) {
            StudentImpl student = students.get(memberId);
            if (student == null) {
                throw new IllegalArgumentException("no such student: " + memberId);
            }
            if (student.hasTeam()) {
                throw new IllegalArgumentException("student already in a team: " + memberId);
            }
            if (!seen.add(memberId)) {
                throw new IllegalArgumentException("student listed twice: " + memberId);
            }
            joining.add(student);
        }

        TeamImpl team = new TeamImpl(nextTeamId, name, target);
        for (StudentImpl student : joining) {
            team.addStudent(student);
        }
        teams.put(nextTeamId, team);
        nextTeamId++;
        return team;
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> free = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (!student.hasTeam()) {
                free.add(student);
            }
        }
        return free;
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        InstructorImpl instructor = instructors.get(instructorId);
        if (instructor == null) {
            return new ArrayList<>();
        }
        List<Student> result = new ArrayList<>();
        for (Workshop workshop : instructor.getWorkshops()) {
            for (Student student : getStudentsOfWorkshop(workshop.getId())) {
                if (!result.contains(student)) {
                    result.add(student);
                }
            }
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop().getId().equals(workshopId)) {
                result.addAll(team.getStudents());
            }
        }
        return result;
    }

    // Both require methods resolve a caller-supplied object to the one this system owns.
    private InstructorImpl require(Instructor instructor) {
        InstructorImpl known = instructor == null ? null : instructors.get(instructor.getId());
        if (known == null) {
            throw new IllegalArgumentException("unknown instructor: " + instructor);
        }
        return known;
    }

    private WorkshopImpl require(Workshop workshop) {
        WorkshopImpl known = workshop == null ? null : workshops.get(workshop.getId());
        if (known == null) {
            throw new IllegalArgumentException("unknown workshop: " + workshop);
        }
        return known;
    }
}
