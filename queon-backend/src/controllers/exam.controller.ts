// src/controllers/exam.controller.ts
import { Request, Response } from "express";
import prisma from "../config/db";
import { createExamSession } from "../services/exam.service";
import { generateQrDataUrl } from "../utils/qr";

// TEMP user id until we add real auth
const DUMMY_USER_ID = "dummy-invigilator-id";

// POST /api/exams
export async function createExamHandler(req: Request, res: Response) {
  try {
    const { examName, room, durationMinutes } = req.body;

    if (!examName || !durationMinutes) {
      return res
        .status(400)
        .json({ message: "examName and durationMinutes are required" });
    }

    const exam = await createExamSession({
      examName,
      room,
      durationMinutes: Number(durationMinutes),
      createdById: DUMMY_USER_ID,
    });

    return res.status(201).json({
      id: exam.id,
      examName: exam.examName,
      room: exam.room,
      durationMinutes: exam.durationMinutes,
      entryToken: exam.entryToken,
      exitToken: exam.exitToken,
      isActive: exam.isActive,
    });
  } catch (err) {
    console.error("Error creating exam:", err);
    return res.status(500).json({ message: "Internal server error" });
  }
}

// POST /api/exams/validate-entry
export async function validateEntryHandler(req: Request, res: Response) {
  try {
    console.log("✅ validateEntryHandler hit");
    const { examId, token } = req.body as {
      examId?: string;
      token?: string;
    };

    if (!examId || !token) {
      return res.status(400).json({
        status: "denied",
        reason: "examId and token are required",
      });
    }

    const exam = await prisma.examSession.findUnique({
      where: { id: examId },
    });

    if (!exam) {
      return res.status(404).json({
        status: "denied",
        reason: "Exam not found",
      });
    }

    if (exam.entryToken !== token) {
      return res.status(401).json({
        status: "denied",
        reason: "Invalid entry token",
      });
    }

    return res.json({
      status: "allowed",
      examId: exam.id,
      examName: exam.examName,
      durationMinutes: exam.durationMinutes,
      message: "Entry token valid. Start exam mode.",
    });
  } catch (err) {
    console.error("Error in validateEntryHandler:", err);
    return res
      .status(500)
      .json({ status: "error", reason: "Internal server error" });
  }
}

// POST /api/exams/validate-exit
export async function validateExitHandler(req: Request, res: Response) {
  try {
    console.log("✅ validateExitHandler hit");
    const { examId, token } = req.body as {
      examId?: string;
      token?: string;
    };

    if (!examId || !token) {
      return res.status(400).json({
        status: "denied",
        reason: "examId and token are required",
      });
    }

    const exam = await prisma.examSession.findUnique({
      where: { id: examId },
    });

    if (!exam) {
      return res.status(404).json({
        status: "denied",
        reason: "Exam not found",
      });
    }

    if (exam.exitToken !== token) {
      return res.status(401).json({
        status: "denied",
        reason: "Invalid exit token",
      });
    }

    return res.json({
      status: "allowed",
      examId: exam.id,
      message: "Exit token valid. End exam mode.",
    });
  } catch (err) {
    console.error("Error in validateExitHandler:", err);
    return res
      .status(500)
      .json({ status: "error", reason: "Internal server error" });
  }
}

// GET /api/exams/:id/qr/entry
export async function getEntryQrHandler(req: Request, res: Response) {
  try {
    const id = req.params.id;

    if (!id) {
      return res
        .status(400)
        .json({ message: "examId (id param) is required" });
    }

    const exam = await prisma.examSession.findUnique({
      where: { id },
    });

    if (!exam) {
      return res.status(404).json({ message: "Exam not found" });
    }

    const payload = {
      examId: exam.id,
      type: "ENTRY",
      token: exam.entryToken,
    };

    const qrDataUrl = await generateQrDataUrl(payload);

    return res.json({
      examId: exam.id,
      type: "ENTRY",
      qrDataUrl,
      rawPayload: payload,
    });
  } catch (err) {
    console.error("Error generating entry QR:", err);
    return res.status(500).json({ message: "Internal server error" });
  }
}

// GET /api/exams/:id/qr/exit
export async function getExitQrHandler(req: Request, res: Response) {
  try {
    const id = req.params.id;

    if (!id) {
      return res
        .status(400)
        .json({ message: "examId (id param) is required" });
    }

    const exam = await prisma.examSession.findUnique({
      where: { id },
    });

    if (!exam) {
      return res.status(404).json({ message: "Exam not found" });
    }

    const payload = {
      examId: exam.id,
      type: "EXIT",
      token: exam.exitToken,
    };

    const qrDataUrl = await generateQrDataUrl(payload);

    return res.json({
      examId: exam.id,
      type: "EXIT",
      qrDataUrl,
      rawPayload: payload,
    });
  } catch (err) {
    console.error("Error generating exit QR:", err);
    return res.status(500).json({ message: "Internal server error" });
  }
}
