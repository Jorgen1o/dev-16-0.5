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
    private static final Path CSV_PATH = AppFiles.STUDENTS_CSV;

    private static final String HEADER =
            "FullName,AcademicStatus,Employed,JobDetails,ProgrammingLanguages,Databases,PreferredRole,Faculty Comment,Whitelisted,Blacklisted";

    private StudentStorage() {}

    private static String norm(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** True if a row with the same full name already exists. */
    public static boolean existsByName(String fullName) throws IOException {
        String key = norm(fullName);
        for (String[] row : readAllRows()) {
            if (row.length > 0 && norm(row[0]).equals(key)) return true; // col 0 = Full Name
        }
        return false;
    }

    /** Append one row (creates file + header if missing). */
    public static void appendRow(String[] row) throws IOException {
        ensureHeader();
        if (row == null || row.length == 0)
            throw new IllegalArgumentException("Empty row");
        if (row[0] == null || row[0].isBlank())
            throw new IllegalArgumentException("Full Name is required");

        // Duplicate
        if (existsByName(row[0])) {
            throw new IllegalStateException("Duplicate student full name: " + row[0]);
        }
        try (Writer w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(CSV_PATH.toFile(), true), StandardCharsets.UTF_8))) {
            w.write(toCsv(row));
            w.write("\n");
        }
    }

    /** Overwrite file with given rows (keeps header). */
    public static void writeAllRows(List<String[]> rows) throws IOException {
        ensureHeader();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String[] r : rows) {
            if (r == null || r.length == 0) continue;
            String key = norm(r[0]);
            if (!seen.add(key)) {
                throw new IllegalStateException("Duplicate student full name in batch: " + r[0]);
            }
        }
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
            StringBuilder record = new StringBuilder();
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                // Append this line to current record
                if (record.length() > 0) record.append("\n");
                record.append(line);

                // If quotes are balanced, we reached the end of the record
                if (isCompleteRecord(record.toString())) {
                    rows.add(parseCsvLine(record.toString()));
                    record.setLength(0);
                }
            }

            // In case last record has no trailing newline
            if (record.length() > 0) {
                rows.add(parseCsvLine(record.toString()));
            }
        }
        return rows;
    }

    /** True if the quoted field count is balanced (record complete). */
    private static boolean isCompleteRecord(String record) {
        long quoteCount = record.chars().filter(ch -> ch == '"').count();
        return quoteCount % 2 == 0;
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

    /** Update an existing student row matched by Full Name. */
    public static void updateStudent(Student s) throws IOException {
        List<String[]> rows = readAllRows();
        boolean updated = false;

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);

            // Match by Full Name only (primary key)
            if (eq(r[0], s.getFullName())) {
                rows.set(i, new String[]{
                        s.getFullName(),
                        s.getAcademicStatus(),
                        s.getEmployed(),
                        s.getJobDetails(),
                        s.getProgrammingLanguages(),
                        s.getDatabases(),
                        s.getPreferredRole(),
                        s.getFacultyComment(),
                        toYesNo(s.getWhiteListed()),
                        toYesNo(s.getBlackListed())
                });
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new IllegalStateException("Student not found to update: " + s.getFullName());
        }

        writeAllRows(rows);
    }

    private static boolean matches(String[] r, Student s) {
        return eq(r[0], s.getFullName());
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

    private static boolean parseBool(String s) {
        if (s == null) return false;
        String t = s.trim().toLowerCase();
        return t.equals("true") || t.equals("yes") || t.equals("y") || t.equals("1");
    }

    static String toYesNo(String s) {
        return parseBool(s) ? "Yes" : "No";
    }
}