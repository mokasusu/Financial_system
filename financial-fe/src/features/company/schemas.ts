import { z } from "zod";

export const companySchema = z.object({
  taxCode: z
    .string()
    .min(1, "Vui lòng nhập mã số thuế")
    .regex(
      /^[0-9]{10}(-[0-9]{3})?$/,
      "Mã số thuế phải đúng định dạng Việt Nam (10 chữ số hoặc 13 chữ số có dấu gạch ngang, VD: 0101234567)"
    ),
  companyName: z
    .string()
    .min(1, "Vui lòng nhập tên doanh nghiệp")
    .max(255, "Tên doanh nghiệp tối đa 255 ký tự"),
  companyNameEn: z.string().max(255).optional().or(z.literal("")),
  shortName: z.string().max(100).optional().or(z.literal("")),
  companyType: z.enum([
    "SINGLE_MEMBER_LIMITED_LIABILITY_COMPANY",
    "MULTI_MEMBER_LIMITED_LIABILITY_COMPANY",
    "JOINT_STOCK_COMPANY",
    "PARTNERSHIP_COMPANY",
    "PRIVATE_ENTERPRISE",
    "OTHER",
  ], {
    error: "Vui lòng chọn loại hình doanh nghiệp",
  }),
  mainBusinessLine: z
    .string()
    .min(1, "Vui lòng nhập ngành nghề kinh doanh chính")
    .max(255, "Ngành nghề tối đa 255 ký tự"),
  establishedDate: z.string().optional().or(z.literal("")),
  legalRepresentative: z.string().max(255).optional().or(z.literal("")),
  address: z
    .string()
    .min(1, "Vui lòng nhập địa chỉ chi tiết trụ sở")
    .max(255, "Địa chỉ tối đa 255 ký tự"),
  ward: z
    .string()
    .min(1, "Vui lòng nhập phường/xã")
    .max(100, "Phường/xã tối đa 100 ký tự"),
  province: z
    .string()
    .min(1, "Vui lòng nhập tỉnh/thành phố")
    .max(100, "Tỉnh/thành phố tối đa 100 ký tự"),
  phone: z
    .string()
    .regex(/^[0-9+()\s-]{7,20}$/, "Số điện thoại không hợp lệ")
    .optional()
    .or(z.literal("")),
  email: z
    .string()
    .email("Định dạng email không hợp lệ")
    .optional()
    .or(z.literal("")),
  website: z.string().max(255).optional().or(z.literal("")),
});

export type CompanyFormData = z.infer<typeof companySchema>;

