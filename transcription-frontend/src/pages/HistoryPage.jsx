import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getHistory } from '../api/transcriptionApi';
import { Clock, FileText } from 'lucide-react';

const HistoryPage = () => {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const royalBlue = '#002D62';
  const saffron = '#FF9933';

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const data = await getHistory();
        setHistory(data); // Use only real backend data
      } catch (err) {
        setError(err.message || 'Could not fetch history data.');
      } finally {
        setLoading(false);
      }
    };

    fetchHistory();
  }, []);

  // -----------------------------
  // Loading State
  // -----------------------------
  if (loading) {
    return (
      <div className="container py-5 text-center">
        <div className="spinner-border text-primary" role="status" style={{ color: royalBlue }}>
          <span className="visually-hidden">Loading...</span>
        </div>
        <p className="mt-3 text-muted">Loading transcription history...</p>
      </div>
    );
  }

  // -----------------------------
  // Error State
  // -----------------------------
  if (error) {
    return (
      <div className="container py-5">
        <div className="alert alert-danger text-center">{error}</div>
      </div>
    );
  }

  // -----------------------------
  // Main Page
  // -----------------------------
  return (
    <div className="container py-5">
      <h2 className="mb-4 fw-bold" style={{ color: royalBlue }}>Transcription History</h2>
      <p className="lead text-muted mb-5">Review all your past audio analysis results.</p>

      {/* No History Found */}
      {history.length === 0 ? (
        <div className="text-center p-5 border rounded-4 shadow-sm bg-light">
          <h4 className="fw-bold mb-3" style={{ color: royalBlue }}>
            No transcriptions found
          </h4>
          <p className="text-muted mb-4">
            You haven't uploaded or analyzed any audio files yet.
          </p>
          <Link
            to="/upload"
            className="btn btn-warning btn-lg fw-bold shadow px-4 py-2"
          >
            🎙️ Upload Your First Audio
          </Link>
        </div>
      ) : (
        // Cards List
        <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
          {history.map((item) => (
            <div key={item.id} className="col">
              <div
                className="card h-100 shadow-sm border-0 rounded-3 border-start border-5"
                style={{ borderColor: saffron }}
              >
                <div className="card-body">
                  <h5 className="card-title text-truncate fw-bold mb-1" style={{ color: royalBlue }}>
                    <FileText size={20} className="me-2 text-muted" />
                    {item.originalFileName}
                  </h5>

                  <p className="card-subtitle mb-2 text-muted small">
                    <Clock size={14} className="me-1" />
                    {new Date(item.createdDate).toLocaleDateString()}
                    {item.duration && ` (${item.duration} min)`}
                  </p>

                  <hr className="my-3" />

                  <p className="text-muted small mb-1">Preview:</p>
                  <p className="card-text text-truncate mb-2">{item.transcriptionPreview}</p>

                  <p className="text-muted small mb-1">Analysis:</p>
                  <span
                    className="badge p-2 rounded-pill"
                    style={{ backgroundColor: royalBlue, color: 'white' }}
                  >
                    {item.analysisPreview.split(':')[0]}
                  </span>

                  <button
                    className="btn btn-sm btn-outline-warning mt-3 w-100 fw-bold"
                    onClick={() => alert(`Showing details for record ${item.id}`)}
                  >
                    View Details
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default HistoryPage;


// import React, { useState, useEffect } from 'react';
// import { getHistory } from '../api/transcriptionApi';
// import { Clock, Download, FileText } from 'lucide-react';

// const HistoryPage = () => {
//   const [history, setHistory] = useState([]);
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState(null);

//   const royalBlue = '#002D62';
//   const saffron = '#FF9933';

//   useEffect(() => {
//     const fetchHistory = async () => {
//       try {
//         const data = await getHistory();
//         // Mock data structure if backend is not live
//         const mockData = data.length > 0 ? data : [
//           { id: 101, originalFileName: "Q1_Earnings_Call.mp3", createdDate: "2024-10-25T10:00:00Z", transcriptionPreview: "The quarterly results show strong growth...", analysisPreview: "Positive sentiment score 92%, focus on Q3 expansion.", duration: 32 },
//           { id: 102, originalFileName: "Client_Meeting_Notes.wav", createdDate: "2024-10-24T15:30:00Z", transcriptionPreview: "We agreed on the scope for the new module...", analysisPreview: "Actionable items listed: 5, Key decision: Module B.", duration: 15 },
//         ];
//         setHistory(mockData);
//       } catch (err) {
//         setError(err.message || 'Could not fetch history data.');
//       } finally {
//         setLoading(false);
//       }
//     };

//     fetchHistory();
//   }, []);

//   if (loading) {
//     return (
//       <div className="container py-5 text-center">
//         <div className="spinner-border text-primary" role="status" style={{ color: royalBlue }}>
//           <span className="visually-hidden">Loading...</span>
//         </div>
//         <p className="mt-3 text-muted">Loading transcription history...</p>
//       </div>
//     );
//   }

//   if (error) {
//     return (
//       <div className="container py-5">
//         <div className="alert alert-danger" role="alert">Error: {error}</div>
//       </div>
//     );
//   }

//   return (
//     <div className="container py-5">
//       <h2 className="mb-4 fw-bold" style={{ color: royalBlue }}>Transcription History</h2>
//       <p className="lead text-muted mb-5">Review all your past audio analysis results.</p>

//       {history.length === 0 ? (
//         <div className="alert alert-info text-center">
//           You have no previous transcriptions. <Link to="/upload" style={{ color: royalBlue }}>Start a new one now.</Link>
//         </div>
//       ) : (
//         <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
//           {history.map((item) => (
//             <div key={item.id} className="col">
//               <div className="card h-100 shadow-sm border-0 rounded-3 border-start border-5" style={{ borderColor: saffron }}>
//                 <div className="card-body">
//                   <h5 className="card-title text-truncate fw-bold mb-1" style={{ color: royalBlue }}>
//                     <FileText size={20} className="me-2 text-muted" />{item.originalFileName}
//                   </h5>
//                   <p className="card-subtitle mb-2 text-muted small">
//                     <Clock size={14} className="me-1" />
//                     {new Date(item.createdDate).toLocaleDateString()}
//                     {item.duration && ` (${item.duration} min)`}
//                   </p>
                  
//                   <hr className="my-3" />

//                   <p className="text-muted small mb-1">Preview:</p>
//                   <p className="card-text text-truncate mb-2">{item.transcriptionPreview}</p>
                  
//                   <p className="text-muted small mb-1">Analysis:</p>
//                   <span className="badge p-2 rounded-pill" style={{ backgroundColor: royalBlue, color: 'white' }}>
//                     {item.analysisPreview.split(':')[0]}
//                   </span>

//                   <button 
//                     className="btn btn-sm btn-outline-warning mt-3 w-100 fw-bold"
//                     onClick={() => alert(`Showing details for record ${item.id}`)}
//                   >
//                     View Details
//                   </button>
//                 </div>
//               </div>
//             </div>
//           ))}
//         </div>
//       )}
//     </div>
//   );
// };

// export default HistoryPage;