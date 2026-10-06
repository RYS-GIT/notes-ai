export interface ChatTurn {
  role: 'user' | 'assistant';
  text: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  sourceNoteIds?: number[];
}

export interface ChatResponse {
  answer: string;
  sourceNoteIds: number[];
}