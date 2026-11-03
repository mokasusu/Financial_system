"use client";

import { useState, useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useStatementStore } from "@/stores/statement-store";
import { pipelineService } from "@/features/statements/services";
import type { DashboardAICommentary } from "../types";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Sparkles,
  RotateCw,
  Edit3,
  Check,
  X,
  Loader2,
  CheckCircle2,
  AlertTriangle,
  RotateCcw,
  Clock,
} from "lucide-react";

interface AiExecutiveCommentaryProps {
  commentary?: DashboardAICommentary;
  statementId?: number;
}

export function AiExecutiveCommentary({
  commentary,
  statementId,
}: AiExecutiveCommentaryProps) {
  const queryClient = useQueryClient();
  const activeStatementId = useStatementStore((state) => state.activeStatementId);
  const targetStatementId = statementId || activeStatementId;

  const hasCommentary = Boolean(commentary?.executiveSummary || commentary?.finalComment);

  const [isEditing, setIsEditing] = useState(false);
  const [editText, setEditText] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [isRegenerating, setIsRegenerating] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState<{ type: "success" | "error"; text: string } | null>(null);

  const executiveSummary = commentary?.executiveSummary;
  const strengths = commentary?.strengths && commentary.strengths.length > 0
    ? commentary.strengths.map((s) => s.description || s.detail || s.title).join("; ")
    : null;
  const recommendations = commentary?.recommendations && commentary.recommendations.length > 0
    ? commentary.recommendations.map((r) => r.description || r.action || r.title).join("; ")
    : null;
  const finalComment = commentary?.finalComment;
  const aiGeneratedComment = commentary?.aiGeneratedComment;

  // Initialize edit text whenever commentary changes
  useEffect(() => {
    if (finalComment) {
      setEditText(finalComment);
    } else if (executiveSummary) {
      setEditText(executiveSummary);
    }
  }, [finalComment, executiveSummary]);

  // Handle Regenerate AI Commentary
  const handleRegenerate = async () => {
    if (!targetStatementId) return;

    try {
      setIsRegenerating(true);
      setFeedbackMsg(null);

      await pipelineService.generateAIByStatement(targetStatementId);

      // Invalidate dashboard queries to fetch freshly generated insights
      await queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
      await queryClient.invalidateQueries({ queryKey: ["analysis-overview"] });

      setFeedbackMsg({
        type: "success",
        text: "Đã kích hoạt tạo lại nhận định AI thành công!",
      });

      // Clear feedback after 4s
      setTimeout(() => setFeedbackMsg(null), 4000);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message ||
        (err as { message?: string })?.message ||
        "Tạo lại nhận định AI thất bại. Vui lòng kiểm tra BCTC đã xác nhận.";

      if (
        msg.includes("currently in progress") ||
        msg.includes("đang tiến hành") ||
        msg.includes("PROCESSING")
      ) {
        setFeedbackMsg({
          type: "success",
          text: "Hệ thống AI đang xử lý phân tích. Kết quả sẽ tự động hiển thị trong giây lát.",
        });
        await queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
        setTimeout(() => setFeedbackMsg(null), 5000);
      } else {
        setFeedbackMsg({ type: "error", text: msg });
      }
    } finally {
      setIsRegenerating(false);
    }
  };

  // Handle Save User Edited Commentary
  const handleSaveEdit = async () => {
    if (!targetStatementId || !editText.trim()) return;

    try {
      setIsSaving(true);
      setFeedbackMsg(null);

      await pipelineService.updateAIFinalCommentByStatement(targetStatementId, editText.trim());

      await queryClient.invalidateQueries({ queryKey: ["dashboard-overview"] });
      await queryClient.invalidateQueries({ queryKey: ["analysis-overview"] });

      setIsEditing(false);
      setFeedbackMsg({
        type: "success",
        text: "Đã lưu chỉnh sửa nhận xét của bạn thành công!",
      });

      setTimeout(() => setFeedbackMsg(null), 4000);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } }; message?: string })?.response?.data?.message ||
        (err as { message?: string })?.message ||
        "Lưu nhận định thất bại. Vui lòng thử lại.";
      setFeedbackMsg({ type: "error", text: msg });
    } finally {
      setIsSaving(false);
    }
  };

  // Reset to original AI commentary
  const handleResetToAiOriginal = () => {
    if (executiveSummary) {
      setEditText(executiveSummary);
    }
  };

  return (
    <Card className="bg-white border-2 border-emerald-500/40 shadow-sm overflow-hidden">
      <CardHeader className="bg-emerald-50/50 py-3 px-5 border-b border-emerald-100 flex flex-row items-center justify-between flex-wrap gap-2">
        <div className="flex items-center gap-2">
          <Sparkles className="h-4 w-4 text-[#006c49]" />
          <CardTitle className="text-xs font-bold uppercase tracking-wider text-[#006c49]">
            NHẬN ĐỊNH CHUYÊN SÂU TỪ AI
          </CardTitle>
          {commentary?.reviewStatus === "EDITED" && (
            <Badge variant="outline" className="text-[10px] bg-amber-50 text-amber-700 border-amber-200 font-semibold">
              ĐÃ CHỈNH SỬA BỞI CHUYÊN VIÊN
            </Badge>
          )}
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          {feedbackMsg && (
            <span
              className={`text-[11px] font-medium px-2 py-0.5 rounded ${
                feedbackMsg.type === "success"
                  ? "bg-emerald-100 text-emerald-800"
                  : "bg-rose-100 text-rose-800"
              }`}
            >
              {feedbackMsg.text}
            </span>
          )}

          {/* Nút Tạo lại nhận xét AI */}
          {(() => {
            const isAiProcessing =
              isRegenerating ||
              commentary?.aiGenerationStatus === "PROCESSING" ||
              commentary?.aiGenerationStatus === "PENDING";

            return (
              <>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={handleRegenerate}
                  disabled={isAiProcessing || !targetStatementId}
                  className="h-7 text-xs font-semibold text-[#006c49] border-emerald-300 hover:bg-emerald-50 gap-1.5"
                  title="Gửi yêu cầu AI phân tích lại toàn bộ các tỷ số và tạo bộ nhận xét mới"
                >
                  <RotateCw className={`h-3 w-3 ${isAiProcessing ? "animate-spin" : ""}`} />
                  {isAiProcessing ? "Đang tạo lại..." : "Tạo lại nhận xét AI"}
                </Button>

                {/* Nút Chỉnh sửa trực tiếp nhận xét */}
                {!isEditing ? (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setIsEditing(true)}
                    className="h-7 text-xs font-semibold text-slate-700 border-slate-300 hover:bg-slate-50 gap-1.5"
                  >
                    <Edit3 className="h-3 w-3 text-[#006c49]" />
                    Chỉnh sửa nhận xét
                  </Button>
                ) : (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setIsEditing(false)}
                    disabled={isSaving}
                    className="h-7 text-xs text-slate-600 gap-1"
                  >
                    <X className="h-3 w-3" />
                    Đóng chỉnh sửa
                  </Button>
                )}

                <Badge
                  variant={
                    hasCommentary
                      ? "safe"
                      : isAiProcessing
                      ? "warning"
                      : commentary?.aiGenerationStatus === "FAILED"
                      ? "destructive"
                      : "outline"
                  }
                  className="text-[10px] uppercase font-semibold"
                >
                  {hasCommentary
                    ? "AI Certified Model"
                    : isAiProcessing
                    ? "ĐANG XỬ LÝ..."
                    : commentary?.aiGenerationStatus === "FAILED"
                    ? "LỖI PHÂN TÍCH"
                    : "CHƯA PHÂN TÍCH"}
                </Badge>
              </>
            );
          })()}
        </div>
      </CardHeader>

      <CardContent className="p-5 space-y-3 text-xs leading-relaxed text-slate-800">
        {/* Chế độ Chỉnh sửa trực tiếp nhận xét */}
        {isEditing ? (
          <div className="space-y-3 bg-slate-50/70 p-4 rounded-xl border border-slate-200 animate-in fade-in">
            <div className="flex items-center justify-between">
              <label className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                <Edit3 className="h-3.5 w-3.5 text-[#006c49]" />
                Chỉnh sửa trực tiếp nhận định chuyên viên (Executive Commentary):
              </label>
              {executiveSummary && (
                <button
                  type="button"
                  onClick={handleResetToAiOriginal}
                  className="text-[11px] text-[#006c49] hover:underline flex items-center gap-1"
                  title="Điền lại văn bản tóm tắt nguyên bản từ AI"
                >
                  <RotateCcw className="h-3 w-3" />
                  Lấy lại văn bản gốc AI
                </button>
              )}
            </div>

            <textarea
              rows={5}
              value={editText}
              onChange={(e) => setEditText(e.target.value)}
              placeholder="Nhập nhận xét, đánh giá chuyên sâu hoặc điều chỉnh các khuyến nghị của chuyên viên..."
              className="w-full rounded-lg border border-slate-300 p-3 text-xs leading-relaxed text-slate-900 bg-white focus:outline-none focus:ring-1 focus:ring-[#006c49] focus:border-[#006c49] font-medium"
            />

            <div className="flex items-center justify-between pt-1">
              <span className="text-[11px] text-slate-400">
                {editText.length} ký tự • Nhận xét này sẽ hiển thị trực tiếp trên Dashboard và Báo cáo PDF chính thức.
              </span>

              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setIsEditing(false)}
                  disabled={isSaving}
                  className="h-8 text-xs"
                >
                  Hủy bỏ
                </Button>

                <Button
                  size="sm"
                  onClick={handleSaveEdit}
                  disabled={isSaving || !editText.trim()}
                  className="h-8 text-xs font-semibold bg-[#006c49] hover:bg-[#00563a] text-white shadow-xs"
                >
                  {isSaving ? (
                    <>
                      <Loader2 className="h-3.5 w-3.5 mr-1.5 animate-spin" />
                      Đang lưu...
                    </>
                  ) : (
                    <>
                      <Check className="h-3.5 w-3.5 mr-1.5" />
                      Lưu nhận xét
                    </>
                  )}
                </Button>
              </div>
            </div>
          </div>
        ) : null}

        {/* Chế độ Hiển thị nhận định */}
        {commentary?.aiGenerationStatus === "PROCESSING" ||
        commentary?.aiGenerationStatus === "PENDING" ||
        isRegenerating ? (
          <div className="py-8 text-center space-y-3 bg-emerald-50/50 rounded-xl border border-emerald-200">
            <Loader2 className="h-8 w-8 text-[#006c49] mx-auto animate-spin" />
            <div className="space-y-1">
              <p className="font-bold text-slate-800 text-sm">
                AI đang xử lý phân tích và tổng hợp nhận định điều hành...
              </p>
              <p className="text-xs text-slate-500 max-w-md mx-auto">
                Hệ thống đang đối chiếu 14 chỉ số tài chính và các quy tắc thẩm định. Màn hình sẽ tự động cập nhật ngay khi hoàn tất.
              </p>
            </div>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-medium">
              <Clock className="h-3.5 w-3.5 animate-pulse" />
              Đang phân tích nền (Tự động cập nhật)
            </div>
          </div>
        ) : hasCommentary ? (
          <>
            {finalComment && (
              <div className="flex items-start gap-2 p-3 rounded-lg bg-emerald-50/80 border border-emerald-200/60 mb-2">
                <span className="font-bold text-[#006c49] shrink-0">• Ý kiến Thẩm định viên:</span>
                <span className="text-slate-800 font-medium">{finalComment}</span>
              </div>
            )}

            {executiveSummary && (
              <div className="flex items-start gap-2">
                <span className="font-bold text-slate-900 shrink-0">• Tóm tắt điều hành:</span>
                <span className="text-slate-700">{executiveSummary}</span>
              </div>
            )}

            {strengths && (
              <div className="flex items-start gap-2">
                <span className="font-bold text-slate-900 shrink-0">• Điểm mạnh cốt lõi:</span>
                <span className="text-slate-700">{strengths}</span>
              </div>
            )}

            {recommendations && (
              <div className="flex items-start gap-2">
                <span className="font-bold text-slate-900 shrink-0">• Cảnh báo & Khuyến nghị:</span>
                <span className="text-slate-700">{recommendations}</span>
              </div>
            )}
          </>
        ) : commentary?.aiGenerationStatus === "FAILED" ? (
          <div className="py-6 text-center text-slate-500 space-y-3 bg-amber-50/50 rounded-xl border border-amber-200">
            <AlertTriangle className="h-7 w-7 text-amber-600 mx-auto" />
            <div className="space-y-1">
              <p className="font-semibold text-slate-800 text-xs">
                Phiên phân tích AI trước đó chưa hoàn tất hoặc bị gián đoạn
              </p>
              <p className="text-[11px] text-slate-500 max-w-md mx-auto">
                Bạn có thể bấm nút bên dưới để hệ thống khởi tạo lại quy trình phân tích và nhận định.
              </p>
            </div>
            <Button
              onClick={handleRegenerate}
              disabled={isRegenerating || !targetStatementId}
              className="bg-[#006c49] hover:bg-[#00563a] text-white text-xs font-semibold h-8 shadow-xs"
            >
              <RotateCw className="h-3.5 w-3.5 mr-1.5" />
              Thử tạo lại nhận định AI
            </Button>
          </div>
        ) : (
          <div className="py-6 text-center text-slate-400 space-y-3">
            <Sparkles className="h-8 w-8 text-[#006c49]/60 mx-auto" />
            <div className="space-y-1">
              <p className="font-semibold text-slate-700 text-xs">
                Chưa có báo cáo nhận định AI cho phiên báo cáo tài chính này
              </p>
              <p className="text-[11px] text-slate-400 max-w-md mx-auto">
                Bấm nút bên dưới để hệ thống AI tự động phân tích cơ cấu tài chính, chấm điểm và tạo nhận định điều hành.
              </p>
            </div>
            <Button
              onClick={handleRegenerate}
              disabled={isRegenerating || !targetStatementId}
              className="bg-[#006c49] hover:bg-[#00563a] text-white text-xs font-semibold h-8 shadow-xs"
            >
              <Sparkles className="h-3.5 w-3.5 mr-1.5" />
              {isRegenerating ? "Đang phân tích..." : "Tạo nhận định AI ngay"}
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

