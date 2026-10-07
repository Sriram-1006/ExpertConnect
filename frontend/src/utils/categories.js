// Must match the Category enum on the backend.
export const CATEGORIES = [
  'TECHNOLOGY',
  'PROGRAMMING',
  'AI_ML',
  'DATABASE',
  'CAREER',
  'FINANCE',
  'LEGAL',
  'HEALTH',
  'EDUCATION',
  'OTHER',
];

export function formatCategory(category) {
  if (!category) return '';
  return category
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ');
}
