package cs151.application;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class StudentStorage {
    // Keep this path consistent for all reads/writes
    private static final Path CSV_PATH = Path.of(System.getProperty("user.home"), "Students.csv");

    private static final String HEADER =
            "FullName,AcademicStatus,Employed,JobDetails,ProgrammingLanguages,Databases,PreferredRole";

    private StudentStorage() {}

    /** Append one row (creates file + header if missing). */
    public static void appendRow(String[] cols) throws IOException {
        ensureHeader();
        try (Writer w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(CSV_PATH.toFile(), true), StandardCharsets.UTF_8))) {
            w.write(toCsv(cols));
            w.write("\n");
        }
    }

    /** Overwrite file with given rows (keeps header). */
    public static void writeAllRows(List<String[]> rows) throws IOException {
        ensureHeader();
        try (Writer w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(CSV_PATH.toFile(), false), StandardCharsets.UTF_8))) {
            w.write(HEADER);
            w.write("\n");
            for (String[] r : rows) {
                w.write(toCsv(r));
                w.write("\n");
            }
        }
    }

    /** Read all rows, skipping the header. */
    public static List<String[]> readAllRows() throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(CSV_PATH)) return rows;
        try (BufferedReader br = Files.newBufferedReader(CSV_PATH, StandardCharsets.UTF_8)) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    /** Delete first row that matches the given Student (all fields). */
    public static void deleteStudent(Student s) throws IOException {
        List<String[]> rows = readAllRows();
        Iterator<String[]> it = rows.iterator();
        while (it.hasNext()) {
            String[] r = it.next();
            if (matches(r, s)) { it.remove(); break; }
        }
        writeAllRows(rows);
    }

    private static boolean matches(String[] r, Student s) {
        if (r.length < 7) return false;
        return eq(r[0], s.getFullName())
                && eq(r[1], s.getAcademicStatus())
                && eq(r[2], s.getEmployed())
                && eq(r[3], s.getJobDetails())
                && eq(r[4], s.getProgrammingLanguages())
                && eq(r[5], s.getDatabases())
                && eq(r[6], s.getPreferredRole());
    }

    private static boolean eq(String a, String b) {
        return Objects.toString(a, "").equals(Objects.toString(b, ""));
    }

    /* ---------- helpers ---------- */

    private static void ensureHeader() throws IOException {
        if (!Files.exists(CSV_PATH) || Files.size(CSV_PATH) == 0) {
            Files.createDirectories(CSV_PATH.getParent());
            Files.writeString(CSV_PATH, HEADER + "\n", StandardCharsets.UTF_8);
        }
    }

    private static String toCsv(String[] cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            String s = cols[i] == null ? "" : cols[i];
            s = s.replace("\"", "\"\""); // escape quotes
            sb.append('"').append(s).append('"');
        }
        return sb.toString();
    }

    private static String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQ) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQ = false;
                } else cur.append(c);
            } else {
                if (c == '"') inQ = true;
                else if (c == ',') { out.add(cur.toString()); cur.setLength(0); }
                else cur.append(c);
            }
        }
        out.add(cur.toString());
        return out.toArray(String[]::new);
    }
}