"use client";

import { useState } from "react";
import { createExam, getEntryQr, getExitQr } from "../src/lib/api";

type ExamResponse = {
  id: string;
  examName: string;
  room?: string | null;
  durationMinutes: number;
  entryToken: string;
  exitToken: string;
  isActive: boolean;
};

type QrResponse = {
  examId: string;
  type: string;
  qrDataUrl: string;
  rawPayload: {
    examId: string;
    type: string;
    token: string;
  };
};

export default function Home() {
  const [examName, setExamName] = useState("");
  const [room, setRoom] = useState("");
  const [durationMinutes, setDurationMinutes] = useState(180);

  const [loading, setLoading] = useState(false);
  const [exam, setExam] = useState<ExamResponse | null>(null);
  const [entryQr, setEntryQr] = useState<QrResponse | null>(null);
  const [exitQr, setExitQr] = useState<QrResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleCreateExam(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setExam(null);
    setEntryQr(null);
    setExitQr(null);

    try {
      const created = await createExam({
        examName,
        room: room || undefined,
        durationMinutes,
      });

      setExam(created);

      const entry = await getEntryQr(created.id);
      const exit = await getExitQr(created.id);

      setEntryQr(entry);
      setExitQr(exit);
    } catch (err: any) {
      console.error(err);
      setError(err.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center py-10 px-4">
      <div className="w-full max-w-3xl bg-slate-900 rounded-2xl p-6 shadow-xl border border-slate-800">
        <h1 className="text-2xl font-bold mb-4 text-slate-50">
          Queon Invigilator Dashboard
        </h1>
        <p className="text-slate-400 mb-6 text-sm">
          Create an exam session to generate ENTRY and EXIT QR codes for students.
        </p>

        <form onSubmit={handleCreateExam} className="space-y-4 mb-6">
          <div>
            <label className="block text-sm mb-1">Exam Name</label>
            <input
              className="w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500"
              value={examName}
              onChange={(e) => setExamName(e.target.value)}
              placeholder="Physics Midterm"
              required
            />
          </div>

          <div>
            <label className="block text-sm mb-1">Room (optional)</label>
            <input
              className="w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500"
              value={room}
              onChange={(e) => setRoom(e.target.value)}
              placeholder="101"
            />
          </div>

          <div>
            <label className="block text-sm mb-1">Duration (minutes)</label>
            <input
              type="number"
              min={1}
              className="w-full rounded-lg bg-slate-800 border border-slate-700 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-sky-500"
              value={durationMinutes}
              onChange={(e) => setDurationMinutes(Number(e.target.value))}
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="mt-2 inline-flex items-center justify-center rounded-lg bg-sky-500 hover:bg-sky-600 disabled:bg-sky-800 px-4 py-2 text-sm font-semibold"
          >
            {loading ? "Creating..." : "Create Exam & Generate QRs"}
          </button>
        </form>

        {error && (
          <div className="mb-4 text-sm text-red-400 bg-red-950/40 border border-red-800 rounded-lg px-3 py-2">
            {error}
          </div>
        )}

        {exam && (
          <div className="mb-6 border border-slate-700 rounded-xl p-4 bg-slate-900/70">
            <h2 className="text-lg font-semibold mb-2">Exam Created</h2>
            <p className="text-sm text-slate-300">
              <span className="font-medium">Name:</span> {exam.examName}
            </p>
            <p className="text-sm text-slate-300">
              <span className="font-medium">Room:</span> {exam.room || "N/A"}
            </p>
            <p className="text-sm text-slate-300">
              <span className="font-medium">Duration:</span> {exam.durationMinutes} minutes
            </p>
            <p className="text-xs text-slate-500 mt-2 break-all">
              <span className="font-medium">Exam ID:</span> {exam.id}
            </p>
          </div>
        )}

        {entryQr && exitQr && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="border border-slate-700 rounded-xl p-4 bg-slate-900/70 flex flex-col items-center">
              <h3 className="font-semibold mb-2 text-sm">ENTRY QR</h3>
              <img
                src={entryQr.qrDataUrl}
                alt="Entry QR"
                className="w-40 h-40 bg-white rounded-md"
              />
              <p className="mt-2 text-xs text-slate-400 text-center">
                Students scan this to enter <br /> exam mode.
              </p>
            </div>
            <div className="border border-slate-700 rounded-xl p-4 bg-slate-900/70 flex flex-col items-center">
              <h3 className="font-semibold mb-2 text-sm">EXIT QR</h3>
              <img
                src={exitQr.qrDataUrl}
                alt="Exit QR"
                className="w-40 h-40 bg-white rounded-md"
              />
              <p className="mt-2 text-xs text-slate-400 text-center">
                Student scans this at submission <br /> to exit exam mode.
              </p>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
