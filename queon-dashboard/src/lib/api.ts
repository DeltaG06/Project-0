
// lib/api.ts
const API_BASE_URL = "http://localhost:4000/api";

export async function createExam(input: {
  examName: string;
  room?: string;
  durationMinutes: number;
}) {
  const res = await fetch(`${API_BASE_URL}/exams`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(input),
  });

  if (!res.ok) {
    throw new Error(`Failed to create exam (${res.status})`);
  }

  return res.json();
}

export async function getEntryQr(examId: string) {
  const res = await fetch(`${API_BASE_URL}/exams/${examId}/qr/entry`);

  if (!res.ok) {
    throw new Error(`Failed to fetch entry QR (${res.status})`);
  }

  return res.json();
}

export async function getExitQr(examId: string) {
  const res = await fetch(`${API_BASE_URL}/exams/${examId}/qr/exit`);

  if (!res.ok) {
    throw new Error(`Failed to fetch exit QR (${res.status})`);
  }

  return res.json();
}
