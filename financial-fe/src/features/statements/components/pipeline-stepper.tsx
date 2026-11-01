"use client";

import {
  Upload,
  Cpu,
  Edit,
  Calculator,
  Sparkles,
  FileCheck,
  Save,
  ChevronRight,
} from "lucide-react";

interface PipelineStepperProps {
  currentStep: number;
  onStepClick?: (step: number) => void;
}

export const PIPELINE_STEPS = [
  { number: 1, label: "Tải lên", icon: Upload },
  { number: 2, label: "OCR", icon: Cpu },
  { number: 3, label: "Rà soát", icon: Edit },
  { number: 4, label: "Tính toán", icon: Calculator },
  { number: 5, label: "AI Đánh giá", icon: Sparkles },
  { number: 6, label: "Hiệu chỉnh", icon: FileCheck },
  { number: 7, label: "Lưu trữ", icon: Save },
];

export function PipelineStepper({
  currentStep,
  onStepClick,
}: PipelineStepperProps) {
  return (
    <div className="rounded-xl bg-white border border-slate-200 p-3 shadow-2xs">
      <div className="flex items-center justify-between overflow-x-auto gap-1 sm:gap-2 pb-1 sm:pb-0">
        {PIPELINE_STEPS.map((step, index) => {
          const Icon = step.icon;
          const isCurrent = currentStep === step.number;
          const isCompleted = currentStep > step.number;
          const isFuture = currentStep < step.number;

          return (
            <div key={step.number} className="flex items-center gap-1 sm:gap-2 shrink-0">
              <button
                type="button"
                disabled={isFuture || !onStepClick}
                onClick={() => {
                  if (isCompleted && onStepClick) {
                    onStepClick(step.number);
                  }
                }}
                className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold shrink-0 transition-all ${
                  isCurrent
                    ? "bg-[#006c49] text-white shadow-xs ring-2 ring-[#006c49]/30"
                    : isCompleted
                    ? "bg-emerald-50 text-emerald-800 hover:bg-emerald-100 cursor-pointer"
                    : "text-slate-400 bg-slate-50 opacity-40 pointer-events-none cursor-not-allowed select-none"
                }`}
              >
                <div
                  className={`h-5 w-5 rounded-full flex items-center justify-center text-[10px] font-bold ${
                    isCurrent
                      ? "bg-white text-[#006c49]"
                      : isCompleted
                      ? "bg-[#006c49] text-white"
                      : "bg-slate-200 text-slate-400"
                  }`}
                >
                  {isCompleted ? "✓" : step.number}
                </div>
                <span className="whitespace-nowrap">{step.label}</span>
              </button>

              {index < PIPELINE_STEPS.length - 1 && (
                <ChevronRight className="h-3.5 w-3.5 text-slate-300 shrink-0 hidden md:block" />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

