import { useState } from "react";
import axios from 'axios';

const AudioUploader = () => {
    const [file, setFile] = useState(null);
    const [transcription, setTranscription] = useState("");

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    const handleUpload = async () => {
        
        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await axios.post('http://localhost:8080/api/transcribe', formData, {
                headers: {
                    'Content-Type':'multipart/form-data',
                }
            });
            setTranscription(response.data);
        } catch (error) {
            console.error("Error transcribing audio", error);
        }
    };

    
    const isUploadDisabled = !file;

    return(
        <div className="container">
            <h1 className="main-title"> Audio to Text Transcriber</h1>
            
            
            <div className="upload-group">
                <div className="file-input-wrapper">
                
                    <input 
                        type="file" 
                        accept="audio/*" 
                        onChange={handleFileChange} 
                        className="actual-input"
                        id="audio-file" 
                    />
                   
                    <label htmlFor="audio-file" className={`file-dropzone ${file ? 'file-selected' : ''}`}>
                        <span className="dropzone-icon">🎙️</span>
                        <p className="dropzone-text">
                            {file 
                                ? `File Selected: ${file.name}` 
                                : "Drag and drop or click to browse audio "
                            }
                        </p>
                    </label>
                </div>
                
                <button 
                    className={`upload-button ${isUploadDisabled ? 'disabled' : ''}`}
                    onClick={handleUpload}
                    disabled={isUploadDisabled}
                >
                    Transcribe Audio
                </button>
            </div>
            
            <div className="transcription-result">
                <h2 className="result-heading">Transcription Result</h2>
                
                <p className="transcription-output">{transcription || "Your transcribed text will appear here after upload."}</p>
            </div>
        </div>
    );
}

export default AudioUploader;