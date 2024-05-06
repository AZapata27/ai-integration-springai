import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { AuthorService } from '../../../services/author.service';
import { Author } from '../../../model/author';
import { BookService } from '../../../services/book.service';
import { Book } from '../../../model/book';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatToolbarModule } from '@angular/material/toolbar';
import { from, switchMap } from 'rxjs';
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { ImageService } from '../../../services/image.service';
import { AudioService } from '../../../services/audio.service';
import { GptService } from '../../../services/gpt.service';

@Component({
  selector: 'app-book-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    ReactiveFormsModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatIconModule
  ],
  templateUrl: './book-dialog.component.html',
  styleUrl: './book-dialog.component.css',
})
export class BookDialogComponent implements OnInit {
  form: FormGroup;
  authors: Author[];
  @ViewChild('autosize') autosize: CdkTextareaAutosize;

  //Forma nativa
  mediaRecorder: MediaRecorder;
  chunks: Blob[] = [];
  audioFile: File;

  //Animacion de picos de audio
  isRecording: boolean = false;
  audioContext: AudioContext;
  analyser: AnalyserNode;
  dataArray: Uint8Array;
  peakBars: number[] = [];
  mediaStream: MediaStream;
  audioSource: MediaStreamAudioSourceNode;
  animationFrameId: number;

  constructor(
    private _dialogRef: MatDialogRef<BookDialogComponent>,
    private authorService: AuthorService,
    private bookService: BookService,
    private gptService: GptService,
    private imageService: ImageService,
    private audioService: AudioService
  ) {}

  ngOnInit() {
    this.form = new FormGroup({
      idBook: new FormControl(0),
      name: new FormControl(''),
      review: new FormControl(''),
      author: new FormControl(),
      urlCover: new FormControl(''),
      enabled: new FormControl(true),
    });

    this.authorService.getAll().subscribe((data) => (this.authors = data));

    //Animacion de picos de audio
    this.audioContext = new AudioContext();
    this.analyser = this.audioContext.createAnalyser();
    this.analyser.fftSize = 256; // Tamaño de la FFT (Fast Fourier Transform)
    this.dataArray = new Uint8Array(this.analyser.frequencyBinCount);

    // Lógica para empezar la grabación y visualizar los picos de audio
    this.visualizeAudio();
  }

  operate() {
    const book = new Book();
    book.idBook = this.form.value['idBook'];
    book.name = this.form.value['name'];
    book.review = this.form.value['review'];
    book.author = this.form.value['author'];
    book.urlCover = this.form.value['urlCover'];
    book.enabled = this.form.value['enabled'];

    this.bookService
      .save(book)
      .pipe(switchMap(() => this.bookService.getAll()))
      .subscribe((data) => this.bookService.setBookChange(data));

    this.closeDialog();
  }

  closeDialog() {
    this._dialogRef.close();
  }

  suggestReview() {
    this.gptService.getText(this.form.value['name']).subscribe((response) => {
      this.form.controls['review'].setValue(response.data);
    });
  }

  suggestCover() {
    this.imageService
      .getImage(this.form.value['name'])
      .subscribe((response) => {
        this.form.controls['urlCover'].setValue(response.data);
      });
  }

  //Forma nativa, Spring AI con Whisper
  startRecording() {
    this.startAudioAnimation();

    navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then((stream) => {
        this.mediaRecorder = new MediaRecorder(stream);
        this.mediaRecorder.ondataavailable = (event) => {
          this.chunks.push(event.data);
        };
        this.mediaRecorder.onstop = () => {
          const blob = new Blob(this.chunks, { type: 'audio/wav' });
          this.audioFile = new File([blob], 'audio.wav', { type: 'audio/wav' });
        };
        this.mediaRecorder.start();
      })
      .catch((error) => {
        console.error('Error al acceder al micrófono:', error);
      });
  }

  stopRecording() {
    this.mediaRecorder.stop(); //detiene la grabacion
    this.stopAudioAnimation();
  }

  startAudioAnimation(){
    //Inicio config de la animacion//
    this.isRecording = true;
    // Conecta el nodo de análisis al destino de salida de audio
    this.audioContext.destination.channelCount = 1;
    this.audioContext.destination.channelCountMode = 'explicit';
    this.audioContext.destination.channelInterpretation = 'discrete';
    this.analyser.connect(this.audioContext.destination);

    // Lógica para empezar la grabación y visualizar los picos de audio
    this.visualizeAudio();
    //Fin config de la animacion//
  }

  stopAudioAnimation() {
    // Detener la captura del audio
    if (this.mediaStream) {
      this.mediaStream.getTracks().forEach((track) => track.stop());
    }
    // Detener la reproducción del audio
    if (this.audioSource) {
      this.audioSource.disconnect();
    }
    // Detener la actualización de la visualización
    cancelAnimationFrame(this.animationFrameId);    
  }

  visualizeAudio() {    
    const captureAudioStream = async () => {
      this.mediaStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
      });
      this.audioSource = this.audioContext.createMediaStreamSource(
        this.mediaStream
      );
      if (this.isRecording) {
        this.audioSource.connect(this.analyser);
      }
    };

    captureAudioStream();

    const updateVisualization = () => {
      if (!this.isRecording) return; // Detener la actualización si la grabación ha terminado
      this.analyser.getByteFrequencyData(this.dataArray);
      this.peakBars = Array.from(this.dataArray);
      this.animationFrameId = requestAnimationFrame(updateVisualization);
    };

    this.animationFrameId = requestAnimationFrame(updateVisualization);
  }


  uploadRecording() {
    const formData = new FormData();
    formData.append('audio', this.audioFile);

    this.audioService
      .uploadAudio(formData)
      .subscribe((response) =>
        this.form.controls['review'].setValue(response.data)
      );
  }

  ngOnDestroy() {
    if(this.isRecording){
      this.stopRecording(); // Asegurarse de detener la grabación al destruir el componente
    }    
  }
}
