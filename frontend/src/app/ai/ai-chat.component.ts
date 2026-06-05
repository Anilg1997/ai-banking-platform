import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AiService, ChatMessage } from '../core/services/ai.service';

@Component({
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

  welcomeMessage: ChatMessage = {
    role: 'assistant',
    content: `👋 Hello! I'm your NovaBank AI Assistant. I can help you with:

• **Account Information** — Check balances, account details
• **Transactions** — View your recent activity
• **Banking FAQ** — Answers to common questions
• **Financial Insights** — Personalized recommendations

How can I help you today?`,
  };

  private destroy$ = new Subject<void>();

  constructor(private aiService: AiService, private sanitizer: DomSanitizer) {}

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

    const history = this.messages.slice(1, -1); // Exclude welcome + current msg
    const lastMessages = history.slice(-10); // Keep last 10 for context

    this.aiService
      .sendMessage(text, lastMessages)
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
            content: response.reply,
          };
          this.messages.push(assistantMsg);

          if (!this.isOpen) {
            this.unreadCount++;
          }
        },
        error: () => {
          this.messages.push({
            role: 'assistant',
            content:
              '⚠️ I apologize, but I\'m having trouble connecting to the AI service. Please ensure Ollama is running locally. You can start it with: `ollama run llama3.2`',
          });
        },
      });
  }

  clearChat(): void {
    this.messages = [this.welcomeMessage];
    this.showWelcome = true;
  }

  suggestPrompt(prompt: string): void {
    this.currentMessage = prompt;
    this.sendMessage();
  }

  get isOllamaAvailable(): boolean {
    // Will be set after a health check
    return true;
  }

  formatMessage(content: string): any {
    // Convert markdown-like formatting to HTML
    let html = content
      .replace(/\n\n/g, '<br><br>')
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/•/g, '&bull;');
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
