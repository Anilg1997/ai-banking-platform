import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface ChatResponse {
  reply: string;
  sources: string[];
  used_rag: boolean;
}

export interface Insight {
  id: string;
  type: 'tip' | 'opportunity' | 'alert';
  title: string;
  message: string;
  icon: string;
  color: string;
  action: { label: string; link: string };
}

export interface AnalysisResult {
  analysis: string;
  type: string;
}

@Injectable({
  providedIn: 'root',
})
export class AiService {
  private directAiUrl = `${environment.aiApiUrl}/api/ai`;

  constructor(private http: HttpClient) {}

  /** Send a chat message to the AI assistant */
  sendMessage(
    message: string,
    history: ChatMessage[] = [],
    useRag: boolean = true
  ): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.directAiUrl}/chat`, {
      message,
      history,
      use_rag: useRag,
    });
  }

  /** Get AI-powered financial insights */
  getInsights(userId?: string): Observable<{ insights: Insight[] }> {
    const params = userId ? `?user_id=${userId}` : '';
    return this.http.get<{ insights: Insight[] }>(
      `${this.directAiUrl}/insights${params}`
    );
  }

  /** Analyze financial data */
  analyze(data: string, analysisType: string = 'general'): Observable<AnalysisResult> {
    return this.http.post<AnalysisResult>(`${this.directAiUrl}/insights/analyze`, {
      data,
      analysis_type: analysisType,
    });
  }

  /** List available MCP banking tools */
  getTools(): Observable<any> {
    return this.http.get(`${this.directAiUrl}/tools/list`);
  }

  /** Call a banking tool */
  callTool(tool: string, arguments: any): Observable<any> {
    return this.http.post(`${this.directAiUrl}/tools/call`, {
      tool,
      arguments,
    });
  }

  /** Check AI service health */
  checkHealth(): Observable<any> {
    return this.http.get(`${this.directAiUrl}/health`);
  }
}
