package patmal.workshops.student.impl.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

/**
 * Unknown keys: lookups never throw. getX(id) returns null and getStudentsOfX(id)
 * returns an empty list for an id that does not exist. Exceptions are used only for
 * operations that would corrupt the system, and are thrown before anything changes.
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    private static final Logger log = LogManager.getLogger(WorkshopSystemImpl.class);

    // Maps give fast lookup by id.
    private final Map<String, InstructorImpl> instructors = new HashMap<>();
    private final Map<String, StudentImpl> students = new HashMap<>();
    private final Map<Integer, WorkshopImpl> workshops = new HashMap<>();
    private final Map<Integer, TeamImpl> teams = new HashMap<>();

    // The JSON exports must list objects in creation order, which a HashMap does not keep.
    // Workshops and teams get sequential ids, so sorting them by id gives that order.
    // Instructor and student ids are chosen by the caller, so their order is recorded here.
    private final List<InstructorImpl> instructorsInCreationOrder = new ArrayList<>();
    private final List<StudentImpl> studentsInCreationOrder = new ArrayList<>();

    private final JsonExporter jsonExporter = new JsonExporter();

    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    public WorkshopSystemImpl() {
    }

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (instructors.containsKey(id)) {
            throw rejected("instructor id already in use: " + id);
        }
        requireValidEmail(email);
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        instructorsInCreationOrder.add(instructor);
        log.info("Created instructor {} ({})", id, name);
        return instructor;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        WorkshopImpl workshop = new WorkshopImpl(nextWorkshopId, name, description);
        workshops.put(nextWorkshopId, workshop);
        nextWorkshopId++;
        log.info("Created workshop {} ({})", workshop.getId(), name);
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        InstructorImpl known = require(instructor);
        WorkshopImpl target = require(workshop);
        target.assignInstructor(known);
        log.info("Workshop {} is now taught by instructor {}", target.getId(), known.getId());
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) {
            throw rejected("student id already in use: " + id);
        }
        requireValidEmail(email);
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        studentsInCreationOrder.add(student);
        log.info("Created student {} ({})", id, name);
        return student;
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> members) {
        WorkshopImpl target = require(workshop);
        if (members == null) {
            throw rejected("a team needs a member list");
        }

        // Validate everything first, so a rejected call changes nothing and consumes no id.
        List<StudentImpl> joining = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String memberId : members) {
            StudentImpl student = students.get(memberId);
            if (student == null) {
                throw rejected("no such student: " + memberId);
            }
            if (student.hasTeam()) {
                throw rejected("student already in a team: " + memberId);
            }
            if (!seen.add(memberId)) {
                throw rejected("student listed twice: " + memberId);
            }
            joining.add(student);
        }

        TeamImpl team = new TeamImpl(nextTeamId, name, target);
        for (StudentImpl student : joining) {
            team.addStudent(student);
        }
        teams.put(nextTeamId, team);
        nextTeamId++;
        log.info("Created team {} ({}) in workshop {} with students {}",
                team.getId(), name, target.getId(), members);
        return team;
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teamsInCreationOrder());
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(studentsInCreationOrder);
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshopsInCreationOrder());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructorsInCreationOrder);
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> free = new ArrayList<>();
        for (StudentImpl student : studentsInCreationOrder) {
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

    @Override
    public void exportInstructorsDataToJson(File targetFileName) {
        jsonExporter.exportInstructors(instructorsInCreationOrder, teamsInCreationOrder(), targetFileName);
        log.info("Exported {} instructors to {}", instructorsInCreationOrder.size(), targetFileName);
    }

    @Override
    public void exportStudentsDataToJson(File targetFileName) {
        jsonExporter.exportStudents(studentsInCreationOrder, teamsInCreationOrder(), targetFileName);
        log.info("Exported {} students to {}", studentsInCreationOrder.size(), targetFileName);
    }

    @Override
    public void exportTeamsDataToJson(File targetFileName) {
        List<TeamImpl> ordered = teamsInCreationOrder();
        jsonExporter.exportTeams(ordered, targetFileName);
        log.info("Exported {} teams to {}", ordered.size(), targetFileName);
    }

    private List<WorkshopImpl> workshopsInCreationOrder() {
        List<WorkshopImpl> ordered = new ArrayList<>(workshops.values());
        ordered.sort(Comparator.comparing(WorkshopImpl::getId));
        return ordered;
    }

    private List<TeamImpl> teamsInCreationOrder() {
        List<TeamImpl> ordered = new ArrayList<>(teams.values());
        ordered.sort(Comparator.comparing(TeamImpl::getId));
        return ordered;
    }

    // Uses commons-validator; a null address is invalid too.
    private void requireValidEmail(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw rejected("invalid e-mail address: " + email);
        }
    }

    // Every rejected operation is logged at warn level before the exception is thrown.
    private IllegalArgumentException rejected(String reason) {
        log.warn("Rejected: {}", reason);
        return new IllegalArgumentException(reason);
    }

    // Both require methods resolve a caller-supplied object to the one this system owns.
    private InstructorImpl require(Instructor instructor) {
        InstructorImpl known = instructor == null ? null : instructors.get(instructor.getId());
        if (known == null) {
            throw rejected("unknown instructor: " + instructor);
        }
        return known;
    }

    private WorkshopImpl require(Workshop workshop) {
        WorkshopImpl known = workshop == null ? null : workshops.get(workshop.getId());
        if (known == null) {
            throw rejected("unknown workshop: " + workshop);
        }
        return known;
    }
}
