"use client";

import { useState } from "react";
import { createExam, getEntryQr, getExitQr } from "../src/lib/api";

export default function Home() {
  const [examName, setExamName] = useState("");
  const [duration, setDuration] = useState(0);
  const [room, setRoom] = useState("");
  const [examId, setExamId] = useState<string | null>(null);
  const [entryQr, setEntryQr] = useState<string | null>(null);
  const [exitQr, setExitQr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleCreateExam() {
    setLoading(true);
    try {
      const exam = await createExam({
        examName,
        room,
        durationMinutes: duration,
      });

      setExamId(exam.id);

      const entry = await getEntryQr(exam.id);
      const exit = await getExitQr(exam.id);

      setEntryQr(entry.qrDataUrl);
      setExitQr(exit.qrDataUrl);
    } catch (error) {
      console.error(error);
      alert("❌ Failed to create exam");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main style={{ padding: "2rem", maxWidth: "500px", margin: "auto" }}>
      <h1 style={{ fontSize: "2rem", fontWeight: "bold", marginBottom: "1rem" }}>
        QUEON Admin Dashboard
      </h1>

      <label>Exam Name</label>
      <input
        style={{ width: "100%", marginBottom: "1rem" }}
        value={examName}
        onChange={(e) => setExamName(e.target.value)}
        placeholder="Physics Midterm"
      />

      <label>Room (optional)</label>
      <input
        style={{ width: "100%", marginBottom: "1rem" }}
        value={room}
        onChange={(e) => setRoom(e.target.value)}
      />

      <label>Duration (minutes)</label>
      <input
        type="number"
        style={{ width: "100%", marginBottom: "1rem" }}
        value={duration === 0 ? "" : duration}
        onChange={(e) =>
          setDuration(e.target.value === "" ? 0 : Number(e.target.value))
        }
      />

      <button
        onClick={handleCreateExam}
        disabled={loading}
        style={{
          width: "100%",
          padding: "1rem",
          background: "#111",
          color: "white",
          cursor: "pointer",
          opacity: loading ? 0.5 : 1,
        }}
      >
        {loading ? "Creating..." : "Create Exam"}
      </button>

      {examId && (
        <>
          <hr style={{ margin: "2rem 0" }} />

          <h2>Exam Created ✔️</h2>
          <p><strong>ID:</strong> {examId}</p>

          <h3>Entry QR</h3>
          <img src={entryQr!} alt="Entry QR" style={{ width: 200 }} />

          <h3 style={{ marginTop: "1rem" }}>Exit QR</h3>
          <img src={exitQr!} alt="Exit QR" style={{ width: 200 }} />
        </>
      )}
    </main>
  );
}
