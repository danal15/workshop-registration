package patmal.workshops.student.impl.app;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

/**
 * workshop-cli: fills a WorkshopSystem with demo data and exports it to
 * instructors.json, students.json and teams.json in the current folder.
 */
public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("workshop-cli starting");
        WorkshopSystem system = new WorkshopSystemImpl();

        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        Workshop java = system.createWorkshop("Java Workshop", "Core Java from the ground up");
        Workshop web = system.createWorkshop("Web Development", "HTML, CSS and JavaScript");
        system.associateWorkshopWithInstructor(dana, java);
        system.associateWorkshopWithInstructor(dana, web);

        system.createStudent("s1", "Alice Cohen", "alice@students.edu");
        system.createStudent("s2", "Bob Friedman", "bob@students.edu");
        system.createStudent("s3", "Carmel Shapiro", "carmel@students.edu");
        system.createStudent("s4", "David Peretz", "david@students.edu");

        system.createTeam("Alpha", java, List.of("s1", "s2"));
        system.createTeam("Beta", web, List.of("s3"));

        log.info("Demo data ready: {} instructors, {} workshops, {} students ({} free), {} teams",
                system.getInstructors().size(), system.getWorkshops().size(), system.getStudents().size(),
                system.getFreeStudents().size(), system.getTeams().size());

        File instructorsFile = new File("instructors.json");
        File studentsFile = new File("students.json");
        File teamsFile = new File("teams.json");
        system.exportInstructorsDataToJson(instructorsFile);
        system.exportStudentsDataToJson(studentsFile);
        system.exportTeamsDataToJson(teamsFile);

        log.info("JSON files written to {}", new File("").getAbsolutePath());
    }
}
