import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AiService, ChatMessage } from '../core/services/ai.service';
import { AgentService, AgentResponse } from '../core/services/agent.service';
import { AuthService } from '../core/auth/auth.service';

@Component({
  standalone: false,
  selector: 'app-ai-chat',
  templateUrl: './ai-chat.component.html',
  styleUrls: ['./ai-chat.component.scss'],
})
export class AiChatComponent implements OnInit, OnDestroy {
  @ViewChild('chatMessages') chatMessagesRef!: ElementRef;

  isOpen = false;
  messages: ChatMessage[] = [];
  currentMessage = '';
  isLoading = false;
  isMinimized = false;
  unreadCount = 0;
  showWelcome = true;
  useRag = true;
  useAgent = false;
  agentMode = 'GENERAL';
  showToolCall = false;

  welcomeMessage: ChatMessage = {
    role: 'assistant',
    content: `Hello! I'm your NovaBank AI Assistant. I can help you with:

\u2022 **Account Information** \u2014 Check balances, account details
\u2022 **Transactions** \u2014 View your recent activity
\u2022 **Banking FAQ** \u2014 Answers to common questions
\u2022 **Financial Insights** \u2014 Personalized recommendations
\u2022 **Card Services** \u2014 Credit card info and management

Toggle **Agent Mode** for AI-powered banking automation!`,
  };

  private destroy$ = new Subject<void>();

  constructor(
    private aiService: AiService,
    private agentService: AgentService,
    public authService: AuthService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.messages.push(this.welcomeMessage);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.unreadCount = 0;
      this.showWelcome = false;
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  sendMessage(): void {
    const text = this.currentMessage.trim();
    if (!text || this.isLoading) return;

    this.showWelcome = false;

    const userMsg: ChatMessage = { role: 'user', content: text };
    this.messages.push(userMsg);
    this.currentMessage = '';
    this.isLoading = true;

    const history = this.messages.slice(1, -1);
    const lastMessages = history.slice(-10);

    if (this.useAgent) {
      this.agentService
        .chat(text, undefined, this.agentMode, this.useRag, true)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false;
            setTimeout(() => this.scrollToBottom(), 50);
          })
        )
        .subscribe({
          next: (response) => {
            let reply = response.reply;
            if (response.usedTools && response.actions?.length > 0) {
              const toolInfo = response.actions
                .filter((a: any) => a.status === 'COMPLETED')
                .map((a: any) => `\u2022 \`${a.toolName}\` completed in ${a.duration}ms`)
                .join('\n');
              if (toolInfo) {
                reply += `\n\n_Agent used tools:_\n${toolInfo}`;
              }
            }
            const assistantMsg: ChatMessage = { role: 'assistant', content: reply };
            this.messages.push(assistantMsg);
            if (!this.isOpen) this.unreadCount++;
          },
          error: () => {
            this.messages.push({
              role: 'assistant',
              content: 'Agent service is not available. Try toggling Agent Mode off to use the standard AI assistant.',
            });
          },
        });
    } else {
      this.aiService
        .sendMessage(text, lastMessages, this.useRag)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isLoading = false;
            setTimeout(() => this.scrollToBottom(), 50);
          })
        )
        .subscribe({
          next: (response) => {
            const assistantMsg: ChatMessage = {
              role: 'assistant',
              content: response.reply + (response.used_rag ? '\n\n_Powered by RAG knowledge base_' : ''),
            };
            this.messages.push(assistantMsg);
            if (!this.isOpen) this.unreadCount++;
          },
          error: () => {
            this.messages.push({
              role: 'assistant',
              content: 'I apologize, but I\'m having trouble connecting to the AI service. Please ensure Ollama is running locally.',
            });
          },
        });
    }
  }

  clearChat(): void {
    this.messages = [this.welcomeMessage];
    this.showWelcome = true;
  }

  suggestPrompt(prompt: string): void {
    this.currentMessage = prompt;
    this.sendMessage();
  }

  toggleRag(): void {
    this.useRag = !this.useRag;
  }

  toggleAgent(): void {
    this.useAgent = !this.useAgent;
    if (this.useAgent) {
      this.messages.push({
        role: 'assistant',
        content: 'Agent Mode activated! I can now use banking tools to fetch real data from your accounts, transactions, and cards. Ask me things like:\n\n\u2022 "What is my account balance?"\n\u2022 "Show my recent transactions"\n\u2022 "How much credit do I have on my card?"\n\u2022 "Analyze my spending patterns"',
      });
    }
  }

  formatMessage(content: string): any {
    let html = content
      .replace(/\n\n/g, '<br><br>')
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\u2022/g, '&bull;')
      .replace(/_(.*?)_/g, '<span class="text-muted">$1</span>');
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  private scrollToBottom(): void {
    try {
      if (this.chatMessagesRef) {
        this.chatMessagesRef.nativeElement.scrollTop =
          this.chatMessagesRef.nativeElement.scrollHeight;
      }
    } catch {}
  }
}
