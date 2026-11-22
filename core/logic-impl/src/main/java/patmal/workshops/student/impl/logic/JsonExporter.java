package patmal.workshops.student.impl.logic;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

/**
 * Writes the three JSON exports with Jackson.
 *
 * The model objects point at each other (a student knows its team, a team knows its students),
 * so they are never handed to Jackson directly. Each export is first copied into small records
 * that hold only the fields of section 5.1 and only point downwards, so the output is a tree
 * by construction. A missing relation stays null and Jackson writes it as the JSON value null.
 */
class JsonExporter {

    // ---- The JSON shapes. Record components become the JSON fields, in this order. ----

    /** The details of a person: exactly id, name and email. */
    record PersonJson(String id, String name, String email) {
    }

    // Instructors export: instructor -> workshops -> teams -> students
    record InstructorJson(String id, String name, String email, List<WorkshopWithTeamsJson> workshops) {
    }

    record WorkshopWithTeamsJson(Integer id, String name, String description, List<TeamWithStudentsJson> teams) {
    }

    record TeamWithStudentsJson(Integer id, String name, List<PersonJson> students) {
    }

    // Students export: student -> team (with student ids) -> workshop -> instructor
    record StudentJson(String id, String name, String email, StudentTeamJson team) {
    }

    record StudentTeamJson(Integer id, String name, List<String> studentIds, WorkshopWithInstructorJson workshop) {
    }

    // Teams export: team -> students, and team -> workshop -> instructor
    record TeamJson(Integer id, String name, List<PersonJson> students, WorkshopWithInstructorJson workshop) {
    }

    /** A workshop with its instructor; used by the students and the teams exports. */
    record WorkshopWithInstructorJson(Integer id, String name, String description, PersonJson instructor) {
    }

    private final ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

    // ---- The three exports. The lists they get are already in creation order. ----

    void exportInstructors(List<? extends Instructor> instructors, List<? extends Team> teams, File target) {
        List<InstructorJson> result = new ArrayList<>();
        for (Instructor instructor : instructors) {
            List<WorkshopWithTeamsJson> workshops = new ArrayList<>();
            for (Workshop workshop : instructor.getWorkshops()) {
                List<TeamWithStudentsJson> teamsOfWorkshop = new ArrayList<>();
                for (Team team : teams) {
                    if (team.getWorkshop().getId().equals(workshop.getId())) {
                        teamsOfWorkshop.add(new TeamWithStudentsJson(team.getId(), team.getName(),
                                persons(team.getStudents())));
                    }
                }
                workshops.add(new WorkshopWithTeamsJson(workshop.getId(), workshop.getName(),
                        workshop.getDescription(), teamsOfWorkshop));
            }
            result.add(new InstructorJson(instructor.getId(), instructor.getName(), instructor.getEmail(),
                    workshops));
        }
        write(result, target);
    }

    void exportStudents(List<? extends Student> students, List<? extends Team> teams, File target) {
        // The Student interface gives only the team name, so find each student's team once.
        Map<String, Team> teamOfStudent = new HashMap<>();
        for (Team team : teams) {
            for (Student member : team.getStudents()) {
                teamOfStudent.put(member.getId(), team);
            }
        }

        List<StudentJson> result = new ArrayList<>();
        for (Student student : students) {
            Team team = teamOfStudent.get(student.getId());
            StudentTeamJson teamJson = null;
            if (team != null) {
                List<String> studentIds = new ArrayList<>();
                for (Student member : team.getStudents()) {
                    studentIds.add(member.getId());
                }
                teamJson = new StudentTeamJson(team.getId(), team.getName(), studentIds,
                        workshopWithInstructor(team.getWorkshop()));
            }
            result.add(new StudentJson(student.getId(), student.getName(), student.getEmail(), teamJson));
        }
        write(result, target);
    }

    void exportTeams(List<? extends Team> teams, File target) {
        List<TeamJson> result = new ArrayList<>();
        for (Team team : teams) {
            result.add(new TeamJson(team.getId(), team.getName(), persons(team.getStudents()),
                    workshopWithInstructor(team.getWorkshop())));
        }
        write(result, target);
    }

    // ---- Helpers ----

    private WorkshopWithInstructorJson workshopWithInstructor(Workshop workshop) {
        return new WorkshopWithInstructorJson(workshop.getId(), workshop.getName(), workshop.getDescription(),
                person(workshop.getInstructor()));
    }

    private List<PersonJson> persons(List<? extends Person> people) {
        List<PersonJson> result = new ArrayList<>();
        for (Person person : people) {
            result.add(person(person));
        }
        return result;
    }

    private PersonJson person(Person person) {
        return person == null ? null : new PersonJson(person.getId(), person.getName(), person.getEmail());
    }

    // Jackson writes files in UTF-8. An existing file is overwritten; a missing folder fails.
    private void write(Object value, File target) {
        try {
            writer.writeValue(target, value);
        } catch (IOException e) {
            throw new UncheckedIOException("could not write " + target, e);
        }
    }
}
