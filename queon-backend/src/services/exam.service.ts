import crypto from "crypto";
import prisma from "../config/db";

function generateToken(prefix: string): string {
  return `${prefix}_${crypto.randomBytes(16).toString("hex")}`;
}

export async function createExamSession(input: {
  examName: string;
  room?: string | null;           // 👈 allow null
  durationMinutes: number;
  createdById: string;
}) {
  const entryToken = generateToken("ent");
  const exitToken = generateToken("ext");

  const exam = await prisma.examSession.create({
    data: {
      examName: input.examName,
      room: input.room ?? null,   // 👈 convert undefined → null
      durationMinutes: input.durationMinutes,
      entryToken,
      exitToken,
      createdById: input.createdById,
    },
  });

  return exam;
}
