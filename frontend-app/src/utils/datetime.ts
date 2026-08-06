// O backend envia LocalDateTime sem informação de fuso, e o servidor grava em UTC.
// Aqui interpretamos o valor como UTC e exibimos sempre no horário de Brasília.
const TIMEZONE = 'America/Sao_Paulo'

function toUtcDate(value: string): Date {
  const hasTimezone = /[zZ]$|[+-]\d{2}:?\d{2}$/.test(value)
  return new Date(hasTimezone ? value : `${value}Z`)
}

export function formatDate(value: string): string {
  if (!value) return ''
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    timeZone: TIMEZONE
  }).format(toUtcDate(value))
}

export function formatDateTime(value: string): string {
  if (!value) return ''
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: TIMEZONE
  }).format(toUtcDate(value))
}
