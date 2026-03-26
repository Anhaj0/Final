// Utility helpers added by Amasha

function isEmpty(value) {
  return value === null || value === undefined || value === "";
}

function isValidString(value) {
  return typeof value === "string" && value.trim().length > 0;
}

function isValidEmail(email) {
  if (!isValidString(email)) return false;
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPhone(phone) {
  if (!isValidString(phone)) return false;
  return /^[0-9]{10}$/.test(phone);
}

function isPositiveNumber(value) {
  return typeof value === "number" && value > 0;
}

function trimValue(value) {
  return typeof value === "string" ? value.trim() : value;
}

function normalizeText(value) {
  if (!isValidString(value)) return "";
  return value.trim().toLowerCase();
}

function capitalizeText(value) {
  if (!isValidString(value)) return "";
  return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
}

function formatName(firstName, lastName) {
  const first = capitalizeText(trimValue(firstName));
  const last = capitalizeText(trimValue(lastName));
  return `${first} ${last}`.trim();
}

function safeParseInt(value) {
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? 0 : parsed;
}

function safeParseFloat(value) {
  const parsed = parseFloat(value);
  return Number.isNaN(parsed) ? 0 : parsed;
}

function generateTimestamp() {
  return new Date().toISOString();
}

function createSuccessResponse(message, data = null) {
  return {
    success: true,
    message,
    data,
    timestamp: generateTimestamp()
  };
}

function createErrorResponse(message, error = null) {
  return {
    success: false,
    message,
    error,
    timestamp: generateTimestamp()
  };
}

function logInfo(message) {
  console.log(`[INFO] ${generateTimestamp()} - ${message}`);
}

function logWarning(message) {
  console.log(`[WARNING] ${generateTimestamp()} - ${message}`);
}

function logError(message) {
  console.log(`[ERROR] ${generateTimestamp()} - ${message}`);
}

function validateRequiredFields(data, requiredFields) {
  const missingFields = [];

  for (const field of requiredFields) {
    if (
      !Object.prototype.hasOwnProperty.call(data, field) ||
      isEmpty(data[field])
    ) {
      missingFields.push(field);
    }
  }

  return {
    isValid: missingFields.length === 0,
    missingFields
  };
}

function sanitizeUserInput(input) {
  if (!isValidString(input)) return "";
  return input.replace(/[<>]/g, "").trim();
}

function sanitizeObjectValues(obj) {
  const sanitized = {};

  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      sanitized[key] =
        typeof obj[key] === "string" ? sanitizeUserInput(obj[key]) : obj[key];
    }
  }

  return sanitized;
}

function getPagination(page = 1, limit = 10) {
  const currentPage = safeParseInt(page) || 1;
  const currentLimit = safeParseInt(limit) || 10;
  const offset = (currentPage - 1) * currentLimit;

  return {
    page: currentPage,
    limit: currentLimit,
    offset
  };
}

function buildSortOptions(sortBy = "createdAt", order = "DESC") {
  const validOrder = order === "ASC" ? "ASC" : "DESC";

  return {
    sortBy,
    order: validOrder
  };
}

function isObject(value) {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function mergeObjects(target, source) {
  if (!isObject(target) || !isObject(source)) {
    return {};
  }

  return { ...target, ...source };
}

function removeEmptyFields(obj) {
  const cleaned = {};

  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key) && !isEmpty(obj[key])) {
      cleaned[key] = obj[key];
    }
  }

  return cleaned;
}

function getStatusLabel(status) {
  const normalizedStatus = normalizeText(status);

  switch (normalizedStatus) {
    case "pending":
      return "Pending";
    case "approved":
      return "Approved";
    case "rejected":
      return "Rejected";
    case "active":
      return "Active";
    case "inactive":
      return "Inactive";
    default:
      return "Unknown";
  }
}

function formatBusData(bus) {
  if (!isObject(bus)) return {};

  return {
    id: bus.id || null,
    plateNumber: sanitizeUserInput(bus.plateNumber || ""),
    route: sanitizeUserInput(bus.route || ""),
    status: getStatusLabel(bus.status || ""),
    createdAt: bus.createdAt || generateTimestamp()
  };
}

function formatComplaintData(complaint) {
  if (!isObject(complaint)) return {};

  return {
    id: complaint.id || null,
    title: sanitizeUserInput(complaint.title || ""),
    description: sanitizeUserInput(complaint.description || ""),
    status: getStatusLabel(complaint.status || ""),
    createdAt: complaint.createdAt || generateTimestamp()
  };
}

function calculatePercentage(value, total) {
  if (!isPositiveNumber(total)) return 0;
  return ((value / total) * 100).toFixed(2);
}

function getSummaryStats(totalItems, resolvedItems) {
  return {
    totalItems,
    resolvedItems,
    unresolvedItems: totalItems - resolvedItems,
    resolvedPercentage: calculatePercentage(resolvedItems, totalItems)
  };
}

function createAuditLog(action, user, details = {}) {
  return {
    action,
    user,
    details,
    timestamp: generateTimestamp()
  };
}

function isArrayWithValues(value) {
  return Array.isArray(value) && value.length > 0;
}

function ensureArray(value) {
  if (Array.isArray(value)) return value;
  if (isEmpty(value)) return [];
  return [value];
}

function uniqueArrayValues(values) {
  return [...new Set(ensureArray(values))];
}

function sortByKey(items, key) {
  if (!Array.isArray(items)) return [];

  return [...items].sort((a, b) => {
    if (a[key] < b[key]) return -1;
    if (a[key] > b[key]) return 1;
    return 0;
  });
}

function filterActiveItems(items) {
  if (!Array.isArray(items)) return [];
  return items.filter(item => normalizeText(item.status) === "active");
}

function mapIds(items) {
  if (!Array.isArray(items)) return [];
  return items.map(item => item.id);
}

function findItemById(items, id) {
  if (!Array.isArray(items)) return null;
  return items.find(item => item.id === id) || null;
}

function createStandardHeaders() {
  return {
    "Content-Type": "application/json",
    "X-App-Name": "TransitShield"
  };
}

function createPaginationResponse(items, page, limit, total) {
  return {
    success: true,
    items,
    pagination: {
      page,
      limit,
      total,
      totalPages: Math.ceil(total / limit)
    },
    timestamp: generateTimestamp()
  };
}

module.exports = {
  isEmpty,
  isValidString,
  isValidEmail,
  isValidPhone,
  isPositiveNumber,
  trimValue,
  normalizeText,
  capitalizeText,
  formatName,
  safeParseInt,
  safeParseFloat,
  generateTimestamp,
  createSuccessResponse,
  createErrorResponse,
  logInfo,
  logWarning,
  logError,
  validateRequiredFields,
  sanitizeUserInput,
  sanitizeObjectValues,
  getPagination,
  buildSortOptions,
  isObject,
  mergeObjects,
  removeEmptyFields,
  getStatusLabel,
  formatBusData,
  formatComplaintData,
  calculatePercentage,
  getSummaryStats,
  createAuditLog,
  isArrayWithValues,
  ensureArray,
  uniqueArrayValues,
  sortByKey,
  filterActiveItems,
  mapIds,
  findItemById,
  createStandardHeaders,
  createPaginationResponse
};