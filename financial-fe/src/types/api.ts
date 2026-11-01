/**
 * Standard API Response envelope matching backend ApiResponse<T>
 */
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
  timestamp: string;
}

/**
 * Standard Pagination Response matching backend PageResponse<T>
 */
export interface PageResponse<T> {
  items: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
}

/**
 * Standardized API Error structure for frontend error handling
 */
export interface ApiError {
  success: false;
  message: string;
  errorCode?: string;
  status?: number;
  timestamp?: string;
}

