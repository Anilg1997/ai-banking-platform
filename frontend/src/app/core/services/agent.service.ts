import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AgentMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface AgentResponse {
  reply: string;
  conversationId: string;
  agentType: string;
  usedRag: boolean;
  usedTools: boolean;
  actions: AgentActionResponse[];
  sources: SourceInfo[];
}

export interface AgentActionResponse {
  actionType: string;
  status: string;
  toolName: string;
  input: string;
  output: string;
  duration: number;
}

export interface SourceInfo {
  title: string;
  content: string;
  category: string;
  similarity: number;
}

export interface AgentConversation {
  id: string;
  userId: string;
  title: string;
  agentType: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  lastActivityAt: string;
}

export interface AgentToolDefinition {
  name: string;
  description: string;
  parameters: any;
}

@Injectable({ providedIn: 'root' })
export class AgentService {
  private apiUrl = `${environment.apiUrl}/api/agent`;

  constructor(private http: HttpClient) {}

  chat(message: string, conversationId?: string, agentType: string = 'GENERAL', useRag: boolean = true, useTools: boolean = true): Observable<AgentResponse> {
    return this.http.post<AgentResponse>(`${this.apiUrl}/chat`, {
      message,
      conversationId,
      agentType,
      useRag,
      useTools
    });
  }

  newConversation(agentType: string = 'GENERAL', title?: string): Observable<AgentConversation> {
    return this.http.post<AgentConversation>(`${this.apiUrl}/conversations/new`, { agentType, title });
  }

  getConversations(): Observable<AgentConversation[]> {
    return this.http.get<AgentConversation[]>(`${this.apiUrl}/conversations`);
  }

  getConversation(id: string): Observable<AgentConversation> {
    return this.http.get<AgentConversation>(`${this.apiUrl}/conversations/${id}`);
  }

  deleteConversation(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/conversations/${id}`);
  }

  analyze(data: string, analysisType: string = 'general'): Observable<any> {
    return this.http.post(`${this.apiUrl}/analyze`, { data, analysisType });
  }

  listTools(): Observable<AgentToolDefinition[]> {
    return this.http.get<AgentToolDefinition[]>(`${this.apiUrl}/tools`);
  }

  checkHealth(): Observable<any> {
    return this.http.get(`${this.apiUrl}/health`);
  }
}
