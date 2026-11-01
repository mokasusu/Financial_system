export type UserStatus = "ACTIVE" | "INACTIVE" | "LOCKED";
export type AuthProvider = "LOCAL" | "GOOGLE";

export interface User {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string | null;
  avatarUrl?: string | null;
  status: UserStatus;
  provider: AuthProvider;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
  expiresIn?: number;
}

export interface AuthResponse extends AuthTokens {
  user: User;
}

export interface LoginRequest {
  email: string;
  password?: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password?: string;
  phoneNumber?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface GoogleAuthRequest {
  idToken: string;
}

