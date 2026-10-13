/**
 * Base relativa da API do LumeOS (mesma origem; o proxy de dev encaminha /api → backend).
 * Centraliza o prefixo para evitar URLs absolutas espalhadas pelos serviços.
 * [REQ-016, REQ-017, RNF-10] (Design §7.2, §7.3)
 */
export const API_BASE = '/api/v1';
