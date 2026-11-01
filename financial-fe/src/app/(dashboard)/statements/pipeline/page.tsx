"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import {
  UploadCloud,
  FileText,
  Loader2,
  AlertCircle,
  ArrowRight,
  ShieldCheck,
  Sparkles,
} from "lucide-react";
import { statementService } from "@/features/statements/services";
import { useCompanyStore } from "@/stores/company-store";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";

import { PipelineStepper } from "@/features/statements/components/pipeline-stepper";

export default function NewStatementPipelinePage() {
  const router = useRouter();
  const currentCompany = useCompanyStore((state) => state.currentCompany);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [enableAi, setEnableAi] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      if (file.size > 50 * 1024 * 1024) {
        setErrorMessage("Dung lượng file vượt quá giới hạn cho phép (Tối đa 50MB).");
        return;
      }
      setSelectedFile(file);
      setErrorMessage(null);
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setErrorMessage("Vui lòng chọn tệp Báo cáo Tài chính dạng PDF hoặc hình ảnh scan.");
      return;
    }

    try {
      setIsLoading(true);
      setErrorMessage(null);

      const res = await statementService.upload(selectedFile, "current", enableAi, true);

      // Redirect immediately to Step 2 (OCR Processing) with the created statement ID
      router.push(`/statements/${res.id}/analysis?step=2`);
    } catch (err: unknown) {
      const message =
        (err as { message?: string })?.message ||
        "Tải lên tệp BCTC thất bại. Vui lòng kiểm tra định dạng file.";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* 1. Thanh tiến trình 7 bước trực quan dính ngay bên dưới header */}
      <PipelineStepper currentStep={1} />

      <Card className="bg-white shadow-xs">
        <CardHeader className="border-b border-slate-100 pb-3">
          <CardTitle className="text-sm font-semibold text-slate-800">
            Tải lên tài liệu Báo cáo Tài chính (PDF / Scan)
          </CardTitle>
          <CardDescription className="text-xs text-slate-500">
            Hỗ trợ định dạng PDF, PNG, JPG, TIFF (Dung lượng tối đa 50MB)
          </CardDescription>
        </CardHeader>

        <CardContent className="p-6">
          <form onSubmit={handleUpload} className="space-y-6">
            {errorMessage && (
              <div className="p-3.5 rounded-lg bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-700 animate-in fade-in duration-200">
                <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
                <div className="flex-1 font-medium">{errorMessage}</div>
              </div>
            )}

            {/* Drag & Drop Area */}
            <div className="border-2 border-dashed border-slate-200 rounded-xl p-8 text-center hover:border-[#006c49] transition-colors bg-slate-50/50">
              <input
                type="file"
                id="statement-file-input"
                className="hidden"
                accept=".pdf,.png,.jpg,.jpeg,.tiff,.bmp"
                onChange={handleFileChange}
                disabled={isLoading}
              />
              <label
                htmlFor="statement-file-input"
                className="cursor-pointer flex flex-col items-center justify-center space-y-3"
              >
                <div className="h-14 w-14 rounded-2xl bg-[#006c49]/10 text-[#006c49] flex items-center justify-center border border-[#006c49]/20">
                  <UploadCloud className="h-7 w-7" />
                </div>

                {selectedFile ? (
                  <div className="space-y-1">
                    <div className="text-sm font-bold text-slate-900 flex items-center justify-center gap-1.5">
                      <FileText className="h-4 w-4 text-[#006c49]" />
                      {selectedFile.name}
                    </div>
                    <p className="text-xs text-slate-500">
                      Dung lượng: {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB • Bấm để chọn file khác
                    </p>
                  </div>
                ) : (
                  <div className="space-y-1">
                    <p className="text-sm font-semibold text-slate-800">
                      Kéo thả file BCTC vào đây hoặc{" "}
                      <span className="text-[#006c49] underline underline-offset-2">chọn từ máy tính</span>
                    </p>
                    <p className="text-xs text-slate-400">
                      Bảng cân đối kế toán, Kết quả kinh doanh và Lưu chuyển tiền tệ
                    </p>
                  </div>
                )}
              </label>
            </div>

            {/* Nhận diện Tự động qua OCR & AI */}
            <div className="rounded-xl bg-emerald-50/70 border border-emerald-200/80 p-4 flex items-start gap-3 text-xs text-emerald-950 shadow-2xs">
              <Sparkles className="h-4 w-4 text-[#006c49] shrink-0 mt-0.5" />
              <div className="space-y-1">
                <p className="font-semibold text-emerald-900">
                  Nhận diện Tự động Chuẩn mực & Niên độ Kế toán
                </p>
                <p className="text-emerald-800/85 leading-relaxed">
                  Hệ thống OCR & AI sẽ tự động đọc, phân loại Chuẩn mực áp dụng (Thông tư 200/2014/TT-BTC hoặc Thông tư 133/2016/TT-BTC) và Kỳ kế toán (Năm tài chính / Quý) trực tiếp từ nội dung văn bản. Bạn không cần thiết lập thủ công.
                </p>
              </div>
            </div>

            <div className="p-3.5 rounded-lg bg-emerald-50/60 border border-emerald-100 flex items-center justify-between text-xs">
              <div className="flex items-center gap-2">
                <ShieldCheck className="h-4 w-4 text-[#006c49]" />
                <span className="text-slate-700">
                  Tự động kích hoạt AI Interpretation & Crop ảnh bằng chứng OCR
                </span>
              </div>
              <input
                type="checkbox"
                checked={enableAi}
                onChange={(e) => setEnableAi(e.target.checked)}
                className="h-4 w-4 rounded border-slate-300 text-[#006c49] focus:ring-[#006c49]"
              />
            </div>

            <div className="pt-2 flex items-center justify-end gap-3 border-t border-slate-100">
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push("/statements")}
                disabled={isLoading}
              >
                Hủy bỏ
              </Button>

              <Button
                type="submit"
                className="bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs px-6 h-10 font-semibold"
                disabled={isLoading || !selectedFile}
              >
                {isLoading ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Đang nạp file & khởi tạo...
                  </>
                ) : (
                  <>
                    Tiếp tục bước 2: Bóc tách OCR
                    <ArrowRight className="h-4 w-4 ml-2" />
                  </>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

