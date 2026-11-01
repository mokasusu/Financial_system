export type CompanyType =
  | "SINGLE_MEMBER_LIMITED_LIABILITY_COMPANY"
  | "MULTI_MEMBER_LIMITED_LIABILITY_COMPANY"
  | "JOINT_STOCK_COMPANY"
  | "PARTNERSHIP_COMPANY"
  | "PRIVATE_ENTERPRISE"
  | "OTHER";

export type CompanyStatus = "ACTIVE" | "INACTIVE" | "PENDING_VERIFICATION";

export interface Company {
  id: number;
  ownerId?: number;
  ownerName?: string;
  taxCode: string;
  companyName: string;
  companyNameEn?: string;
  shortName?: string;
  companyType: CompanyType;
  companyTypeDisplayName?: string;
  mainBusinessLine: string;
  establishedDate?: string;
  legalRepresentative?: string;
  address: string;
  ward: string;
  province: string;
  phone?: string;
  email?: string;
  website?: string;
  status: CompanyStatus;
  statusDisplayName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateCompanyRequest {
  taxCode: string;
  companyName: string;
  companyNameEn?: string;
  shortName?: string;
  companyType: CompanyType;
  mainBusinessLine: string;
  establishedDate?: string;
  legalRepresentative?: string;
  address: string;
  ward: string;
  province: string;
  phone?: string;
  email?: string;
  website?: string;
}

