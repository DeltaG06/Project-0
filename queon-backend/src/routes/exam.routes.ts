// src/routes/exam.routes.ts
import { Router } from "express";
import {
  createExamHandler,
  validateEntryHandler,
  validateExitHandler,
  getEntryQrHandler,
  getExitQrHandler,
} from "../controllers/exam.controller";

const router = Router();

console.log("🧭 exam.routes.ts loaded");

// Create exam
router.post("/exams", createExamHandler);

// Validate entry (when student scans ENTRY QR)
router.post("/exams/validate-entry", validateEntryHandler);

// Validate exit (when student scans EXIT QR)
router.post("/exams/validate-exit", validateExitHandler);

// Get QR for entry / exit
router.get("/exams/:id/qr/entry", getEntryQrHandler);
router.get("/exams/:id/qr/exit", getExitQrHandler);

export default router;
