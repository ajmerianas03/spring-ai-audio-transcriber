
import React, { useState } from 'react';
import { uploadAudio } from '../api/transcriptionApi';
import { FileUp, TrendingUp, BookOpen } from 'lucide-react';

const UploadPage = () => {
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const royalBlue = '#002D62';
  const saffron = '#FF9933';

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
    setResult(null); // Clear previous results
    setError(null);
  };

  // const handleSubmit = async (e) => {
  //   e.preventDefault();
  //   if (!file) {
  //     setError('Please select an audio file to upload.');
  //     return;
  //   }

  //   setLoading(true);
  //   setError(null);
    
  //   try {
  //     const response = await uploadAudio(file);
  //     console.log('API Response:', response);
  //     setResult(response);
  //     setFile(null); // Clear file input after success
  //   } catch (err) {
  //     const errorMessage = err.message || 'File upload failed. Check file size and format.';
  //     setError(errorMessage);
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  const handleSubmit = async (e) => {
  e.preventDefault();
  if (!file) {
    setError('Please select an audio file to upload.');
    return;
  }

  setLoading(true);
  setError(null);

  try {
    const response = await uploadAudio(file);
    console.log('API Response:', response); // Debugging

    // Assuming response has "analysis", "transcription", and "recordId"
    const result = {
      transcriptionText: response.transcription,  // Use transcription field
      summary: extractSummary(response.analysis), // Extract summary from analysis
      keywords: extractKeywords(response.transcription), // Extract keywords (if applicable)
      recordId: response.recordId,
    };
    setResult(result);
    setFile(null); // Clear file input after success
  } catch (err) {
    const errorMessage = err.message || 'File upload failed. Check file size and format.';
    setError(errorMessage);
  } finally {
    setLoading(false);
  }
};

// Helper to extract summary from analysis
const extractSummary = (analysis) => {
  const summaryIndex = analysis.indexOf('Summary:');
  return summaryIndex !== -1 ? analysis.substring(summaryIndex + 9).trim() : 'No summary available.';
};

// You can add logic to extract keywords from transcription if needed.
const extractKeywords = (transcription) => {
  // Example: Return first 5 words as a basic keyword list
  const words = transcription.split(' ').slice(0, 5);
  return words.length > 0 ? words : [];
};

  return (
    <div className="container py-5">
      <h2 className="mb-4 fw-bold" style={{ color: royalBlue }}>Audio Transcription Service</h2>
      
      {/* Upload Form */}
      <div className="card p-4 mb-5 shadow-sm rounded-4 border-1" style={{ borderColor: saffron }}>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label htmlFor="audioFile" className="form-label fw-bold" style={{ color: royalBlue }}>
              Select Audio File (mp3, wav, etc.)
            </label>
            <input 
              type="file" 
              className="form-control" 
              id="audioFile" 
              accept="audio/*"
              onChange={handleFileChange} 
              disabled={loading}
              required
            />
          </div>
          
          {file && (
            <p className="text-muted small">Ready to upload: **{file.name}**</p>
          )}

          {error && (
            <div className="alert alert-danger" role="alert">{error}</div>
          )}
          
          <button 
            type="submit" 
            className="btn btn-lg fw-bold shadow-sm"
            style={{ backgroundColor: saffron, color: royalBlue }}
            disabled={loading || !file}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Processing...
              </>
            ) : (
              <>
                <FileUp size={20} className="me-2" /> Transcribe and Analyze
              </>
            )}
          </button>
        </form>
      </div>

      {/* Transcription Results Display */}
      {result && (
        <div className="card shadow-lg rounded-4 p-4" style={{ backgroundColor: '#f9f9f9' }}>
          <h3 className="mb-4 fw-bold" style={{ color: royalBlue }}>Analysis Complete!</h3>
          
          {/* Summary Card (Saffron Accent) */}
          <div className="card mb-4 border-0 border-start border-5 rounded-3" style={{ borderColor: saffron }}>
            <div className="card-body bg-light">
              <h5 className="card-title mb-3 fw-bold" style={{ color: saffron }}>
                <BookOpen size={20} className="me-2" /> AI Summary
              </h5>
              <p className="card-text">{result.summary || 'Summary not available.'}</p>
            </div>
          </div>
          
          {/* Keywords/Tags */}
          <div className="mb-4">
            <h5 className="mb-3 fw-bold" style={{ color: royalBlue }}>
              <TrendingUp size={20} className="me-2" /> Key Topics/Keywords
            </h5>
            <div>
              {(result.keywords && result.keywords.length > 0) ? (
                result.keywords.map((kw, index) => (
                  <span key={index} className="badge bg-primary me-2 mb-1 rounded-pill p-2" style={{ backgroundColor: royalBlue }}>
                    {kw}
                  </span>
                ))
              ) : (
                <span className="text-muted">No keywords identified.</span>
              )}
            </div>
          </div>

          {/* Full Transcription */}
          <h5 className="mb-3 fw-bold" style={{ color: royalBlue }}>Full Transcript</h5>
          <div className="p-3 border rounded bg-white" style={{ maxHeight: '300px', overflowY: 'auto' }}>
            <p className="text-muted mb-0" style={{ whiteSpace: 'pre-wrap' }}>
              {result.transcriptionText || 'Transcription text not available.'}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default UploadPage;