import { NgClass } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GptService } from '../../services/gpt.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule, NgClass],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit{

  messages: { text: string, sender: string }[] = [];
  newMessage: string = '';

  constructor(private gptService: GptService) { }

  ngOnInit(): void {
    // Puedes inicializar mensajes aquí o cargarlos desde una fuente de datos.
    this.messages.push({text: 'Hola soy Booki, tu asistente virtual, ¿en qué puedo ayudarte hoy?', sender: 'bot'});
  }

  sendMessage() {
    if (this.newMessage.trim() !== '') {
      this.messages.push({ text: this.newMessage, sender: 'user' });
      // Aquí podrías agregar lógica para enviar el mensaje al servidor o procesarlo de alguna manera.
      this.gptService.chat(this.newMessage).subscribe(response => {
        //console.log(response);
        this.messages.push({text: response.data, sender: 'bot'});
        this.newMessage = '';
      });
    }
  }
}
