import { z } from "zod";

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, "Vui lòng nhập địa chỉ email")
    .email("Định dạng email không hợp lệ"),
  password: z
    .string()
    .min(6, "Mật khẩu phải chứa ít nhất 6 ký tự"),
  rememberMe: z.boolean().optional(),
});

export type LoginFormData = z.infer<typeof loginSchema>;

export const registerSchema = z
  .object({
    fullName: z
      .string()
      .min(2, "Họ và tên phải từ 2 ký tự trở lên")
      .max(100, "Họ và tên không vượt quá 100 ký tự"),
    email: z
      .string()
      .min(1, "Vui lòng nhập địa chỉ email")
      .email("Định dạng email không hợp lệ"),
    phoneNumber: z
      .string()
      .regex(/^[0-9+()\\s-]{7,20}$/, "Số điện thoại không hợp lệ")
      .optional()
      .or(z.literal("")),
    password: z
      .string()
      .min(6, "Mật khẩu phải từ 6 ký tự trở lên")
      .max(100, "Mật khẩu không vượt quá 100 ký tự"),
    confirmPassword: z.string().min(6, "Vui lòng xác nhận mật khẩu"),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Mật khẩu xác nhận không trùng khớp",
    path: ["confirmPassword"],
  });

export type RegisterFormData = z.infer<typeof registerSchema>;

